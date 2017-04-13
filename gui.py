import Tkinter, Tkconstants, tkFileDialog, tkMessageBox
import provisional_certificate
from sys import platform

class MainWindow(Tkinter.Frame):

  def __init__(self, root):

    Tkinter.Frame.__init__(self, root)

    # options for buttons
    button_opt = {'fill': Tkconstants.BOTH, 'padx': 5, 'pady': 5}
    self.file_opt = {}
    self.file_opt['defaultextension'] = '.xls'
    # self.file_opt['filetypes'] = [('all files', '.*'), ('text files', '.txt'), ('Excel files', '.xls*')]
    self.file_opt['initialdir'] = '/Users/' if platform == "darwin" else 'C:\\Users\\'
    self.file_opt['initialfile'] = 'input.xls'
    self.file_opt['parent'] = root

    # define widgets

    Tkinter.Label(self, text="Input file path:").pack()
    self.filenameinput = Tkinter.Entry(self)
    self.filenameinput.pack()
    Tkinter.Button(self, text='Select Input file', command=self.choose_input_file).pack(**button_opt)
    Tkinter.Label(self, text="Enter private key alias").pack()
    self.aliasinput = Tkinter.Entry(self)
    self.aliasinput.insert(Tkinter.END, 'le-9f8bfd35-9ccd-41d2-b396-b25def9e02b3')
    self.aliasinput.pack()
    Tkinter.Label(self, text="Enter token password").pack()
    self.passwordinput = Tkinter.Entry(self, show="*")
    self.passwordinput.pack()
    Tkinter.Label(self, text="Enter signing reason").pack()
    self.reasoninput = Tkinter.Entry(self)
    self.reasoninput.pack()
    Tkinter.Label(self, text="Enter signing location").pack()
    self.locationinput = Tkinter.Entry(self)
    self.locationinput.pack()
    Tkinter.Button(self, text='Create Priovisional Certificates', command=self.create_provisional_certificates).pack(**button_opt)
    # define options for opening or saving a file

  def choose_input_file(self):

    """Returns an opened file in read mode.
    This time the dialog just returns a filename and the file is opened by your own code.
    """
    filename = tkFileDialog.askopenfilename(**self.file_opt)
    self.filenameinput.delete(0, Tkinter.END)
    self.filenameinput.insert(0,filename)
    return

  def create_provisional_certificates(self):
    inputfilename = self.filenameinput.get()
    alias = self.aliasinput.get()
    reason = self.reasoninput.get()
    location = self.locationinput.get()
    password = self.passwordinput.get()
    res = provisional_certificate.main(['create_sign_script', inputfilename, password, alias, reason, location])
    if res == 0:
        tkMessageBox.showinfo("Success!", "Successfully created signed provisional certificates.")
    else:
        tkMessageBox.showinfo("Certificate creation failed. Stack trace: " + res)
    return



if __name__=='__main__':
  root = Tkinter.Tk()
  MainWindow(root).pack()
  root.mainloop()