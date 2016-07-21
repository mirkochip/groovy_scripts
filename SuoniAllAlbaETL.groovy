import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPHTTPClient

/* connection parameters */

ftpServer = '91.121.154.65'
username = 'suonialba'
password = 'password'

radioFtpServer = '192.240.97.69'
radioUsername = 'cmusumec'
radioPassword = 'radioPassword'

/* file system parameters */

sourceDir = "/suoni_allalba"

file1 = 'suoni_allalba1.mp3'
tmpFile1 = 'tmpFile1.mp3'

file2 = 'suoni_allalba2.mp3'
tmpFile2 = 'tmpFile2.mp3'

file3 = 'suoni_allalba3.mp3'
tmpFile3 = 'tmpFile3.mp3'

destDir = '/media/Trasmissioni/Suoni_Alba'

mvFileCmd1 = """mv ${file1} ${tmpFile1}"""
mvFileCmd2 = """mv ${file2} ${tmpFile2}"""
mvFileCmd3 = """mv ${file3} ${tmpFile3}"""

/* ffmpeg conversion commands */

convertBitRateCmd1 = ['ffmpeg', '-i', tmpFile1, '-ab', '128k', file1]
convertBitRateCmd2 = ['ffmpeg', '-i', tmpFile2, '-ab', '128k', file2]
convertBitRateCmd3 = ['ffmpeg', '-i', tmpFile3, '-ab', '128k', file3]

/* ETL steps */

downloadStep()
//convertStep()
uploadStep()


def downloadStep() {
    new FTPClient().with {
    //new FTPHTTPClient('proxy', 80, 'user', 'password').with {

        // establish a connection with Source FTP - download step

        connect ftpServer
        enterLocalPassiveMode()
        login username, password

        changeWorkingDirectory sourceDir

        // download part 1
	println 'downloading part 1'
        incomingFile = new File(file1)
        incomingFile.withOutputStream { ostream -> retrieveFile file1, ostream }
	println 'downloaded part 1'

        // download part 2
	println 'downloading part 2'
        incomingFile = new File(file2)
        incomingFile.withOutputStream { ostream -> retrieveFile file2, ostream }
	println 'downloaded part 2'

        // download part 3
	println 'downloading part 3'
        incomingFile = new File(file3)
        incomingFile.withOutputStream { ostream -> retrieveFile file3, ostream }
	println 'downloaded part 3'

        disconnect()
    }

}

def convertStep () {

    // converting mp3s to 128k bit rate

    def mvFile1 = mvFileCmd1.execute()
    mvFile1.waitFor()

    def proc1 = new ProcessBuilder(convertBitRateCmd1).start()
    proc1.consumeProcessErrorStream(System.err)
    proc1.consumeProcessOutputStream(System.out)
    proc1.waitFor()

    def mvFile2 = mvFileCmd2.execute()
    mvFile2.waitFor()

    def proc2 = new ProcessBuilder(convertBitRateCmd2).start()
    proc2.consumeProcessErrorStream(System.err)
    proc2.consumeProcessOutputStream(System.out)
    proc2.waitFor()

    def mvFile3 = mvFileCmd3.execute()
    mvFile3.waitFor()

    def proc3 = new ProcessBuilder(convertBitRateCmd3).start()
    proc3.consumeProcessErrorStream(System.err)
    proc3.consumeProcessOutputStream(System.out)
    proc3.waitFor()

}

def uploadStep() {

    new FTPClient().with {
    //new FTPHTTPClient('proxy', 80, 'user', 'password').with {

        // establish a connection with HMC FTP - upload step

        connect radioFtpServer
        enterLocalPassiveMode()
        login radioUsername, radioPassword

        changeWorkingDirectory destDir

        setFileType(BINARY_FILE_TYPE, BINARY_FILE_TYPE)
        setFileTransferMode(BINARY_FILE_TYPE)

        // upload

	println 'uploading part 1'
        storeFile(file1, new FileInputStream(file1))
	println 'uploading part 2'
        storeFile(file2, new FileInputStream(file2))
	println 'uploading part 3'
        storeFile(file3, new FileInputStream(file3))

        disconnect()

    }
}