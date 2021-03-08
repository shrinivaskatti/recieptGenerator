@echo on
PowerShell -NoProfile -ExecutionPolicy Bypass -Command "& 'F:\Study\NilayaFoundation\bulkEmail.ps1'" -email nilaya.foundation@gmail.com -password PVN@DWD2020 -importFile "F:\Study\NilayaFoundation\Users.csv" -emailContent "F:\Study\NilayaFoundation\thankYouEmail.txt"
pause