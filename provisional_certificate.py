
from xlrd import open_workbook
from reportlab.pdfgen import canvas
from reportlab.platypus import Paragraph
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.units import inch
from reportlab.lib.enums import TA_JUSTIFY
from reportlab.lib.colors import black
from reportlab.lib.pagesizes import letter, A4
import os
import sys
from datetime import date

def main(argv):
	if len(argv) > 1:
		filename = argv[1]
	else:
		filename = "input_formats/provisional_certificate.xls"
	wb = open_workbook(filename=filename, on_demand=True)
	sheet = wb.sheet_by_index(0)
	for row_number in xrange(1,sheet.nrows):
		htno = sheet.cell(row_number, 1).value
		name = sheet.cell(row_number, 2).value
		father_name = sheet.cell(row_number, 3).value
		mother_name = sheet.cell(row_number, 4).value
		month_of_passing = sheet.cell(row_number, 5).value
		year_of_passing = sheet.cell(row_number, 6).value
		percentage = sheet.cell(row_number, 7).value
		branch_code = sheet.cell(row_number, 10).value
		pc_track_no = sheet.cell(row_number, 11).value
		gender = sheet.cell(row_number, 13).value
		cmm_track_no = sheet.cell(row_number, 15).value
		if branch_code == "01":
			branch = "Civil Engineering"
		elif branch_code == "02":
			branch = "Electrical and Electronics Engineering"
		elif branch_code == "03":
			branch = "Mechanical Engineering"
		elif branch_code == "04":
			branch = "Electronics and Communication Engineering"
		elif branch_code == "05":
			branch = "Computer Science and Engineering"
		elif branch_code == "10":
			branch = "Electronics and Instrumentation Engineering"
		elif branch_code == "12":
			branch = "Information Technology"
		else:
			raise ValueError
		class_string = "PASS CLASS"
		if percentage >= 70:
			class_string = "FIRST CLASS WITH DISTINCTION"
		elif percentage >= 60:
			class_string = "FIRST CLASS"
		elif percentage >= 50:
			class_string = "SECOND CLASS"
		cnv = canvas.Canvas("output/" + name + "_pc.pdf", pagesize=A4)
		cnv.drawImage('templates/provisional_certificate.jpg', 0, 0, height = A4[1], width = A4[0])
		cnv.drawString(1.2 * inch, 10.25 * inch, pc_track_no)
		cnv.drawString(5.6 * inch, 9.85 * inch, htno)
		cnv.drawString(5.6 * inch, 9.4 * inch, cmm_track_no)
		cnv.drawString(1.25 * inch, 0.45 * inch, date.today().strftime('%d, %b %Y'))
		style = ParagraphStyle(
            'Body Text',
            fontName='Helvetica',
            fontSize=20,
            leading=24,
            leftIndent=0,
            rightIndent=0,
            firstLineIndent=0,
            alignment=TA_JUSTIFY,
            spaceBefore=0,
            spaceAfter=0,
            bulletFontName='Helvetica',
            bulletFontSize=10,
            bulletIndent=0,
            textColor= black,
            backColor=None,
            wordWrap=None,
            borderWidth= 0,
            borderPadding= 0,
            borderColor= None,
            borderRadius= None,
            allowWidows= 1,
            allowOrphans= 0,
            textTransform=None,
            endDots=None,         
            splitLongWords=1,
        )
		p = Paragraph("This is to certify that <b>" + ("Mr. " if gender == "M" else "Ms. ") + name + "</b><br/>" + ("son" if gender == "M" else "daughter") + " of <b>Mr. " + father_name + "</b> and <b>Mrs. " + mother_name + "</b><br/>passed <b>B.TECH. " + branch + "</b> examination of the JNTUH, Hyderabad, held in " + month_of_passing + ", " + year_of_passing + "<br/>placed in <b>" + class_string + "</b>.<br/>Student has satisfied all the requirements for the award of the degree.",style)
		p.wrap(7.4 * inch, 4.5 * inch)
		p.drawOn(cnv, 0.5 * inch, 5 * inch)
		cnv.showPage()
		cnv.save()
		if len(argv) == 5:
			sign_password = argv[2]
			sign_location = argv[3]
			sign_reason = argv[4]
			alias = argv[5]
		else:
			sign_password = "rupee@123"
			sign_location = "CVR College"
			sign_reason = "Authentication and Non-repudation"
			alias = "le-9f8bfd35-9ccd-41d2-b396-b25def9e02b3"
		if sys.platform == "darwin":
			res = os.system("java -jar java/SignWithUSB.jar \"output/" + name + "_pc.pdf\" \"output/" + name + "_pc_signed.pdf\" /usr/local/lib/libeTPkcs11.dylib \""+sign_password+"\" \""+alias+"\" \"" + sign_reason + "\" \"" + sign_location + "\"")
		else:
			res = os.system("java -jar \"java\\SignWithUSB.jar\" \"output\\" + name + "_pc.pdf\" \"output\\" + name + "_pc_signed.pdf\" \"C:\\Windows\\System\\eTPkcs11.dll\" \""+sign_password+"\" \""+alias+"\" \"" + sign_reason + "\" \"" + sign_location + "\"")
	return res


if __name__ == "__main__":
    main(sys.argv)