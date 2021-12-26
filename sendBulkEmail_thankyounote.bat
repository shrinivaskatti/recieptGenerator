@echo on
PowerShell -NoProfile -ExecutionPolicy Bypass -Command "& 'D:\Receipt\recieptGenerator-main\bulkEmail.ps1'" -email nilaya.foundation@gmail.com -password PVN@DWD2020 -importFile "F:\Study\NilayaFoundation\Users.csv" -emailContent "D:\Receipt\recieptGenerator-main\thankYouEmail.txt" > OutputLogs.txt
pause