
param(
	 [Parameter(Mandatory=$true)][string]$email,
	 [Parameter(Mandatory=$true)][string]$password,
	 [Parameter(Mandatory=$true)][string]$emailContent,
	 [Parameter(Mandatory=$true)][string]$importFile  

)
$importFile     = "F:\Study\NilayaFoundation\2020-21\donorOutput.csv"

$users = import-csv $importFile | select *
foreach($user in $users)
{
    
    $Name           = $user.'Name'
    $to             = $user.'Email'
	$Reciept		= $user.'Reciept'
	$VolunteerEmail = $user.'VolunteerEmail'
   <# $to2        = $user.'To2'
    $Attachment    = $user.'Attachment'
    $Attachment2   = $user.'Attachment2'    
	#>
	if([string]::IsNullOrEmpty($to)){
	
			if([string]::IsNullOrEmpty($VolunteerEmail)){
			
				Write-Host "Email address not found for" $Name"."
			}else{
				$to = $VolunteerEmail
			}
	}
	if([string]::IsNullOrEmpty($to)){
	 Write-Host "Skipping Donor :" $Name "."
	}else{
		
		
		
		$write = "Emailing account " + $to + " ..."
		Write-Host $write
		Write-Host $Reciept
		$body = [string]::join([environment]::newline, (Get-Content -path $emailContent))

		$body = $body.Replace('[Name]', $Name)

		$mail = New-Object System.Net.Mail.MailMessage 
		#$mail.From = "shrinivaskattiaws@gmail.com" 
		$mail.From = $email 
		
		<#if($recipientType -eq "To")
		{
			$mail.To.Add($to)
			Write-Host "To Address"
		}elseif($recipientType -eq "Cc"){
			Write-Host "CC Address"
			$mail.Cc.Add("shrinivaskattiaws@gmail.com")
		}else{
			$mail.Bcc.Add("shrinivaskattiaws@gmail.com")
		}#>
		
		$mail.To.Add($to)
		$mail.Cc.Add($VolunteerEmail)
		#$mail.Bcc.Add("shrinivaskattiaws@gmail.com")
		<#$mail.To.Add($to2)#>
		$mail.Subject = "Test Email : With Content"
		$mail.IsBodyHtml = $true
		$mail.Body = $body
		$attach = New-Object System.Net.Mail.Attachment($Reciept) 
		$mail.Attachments.Add($attach)
		<#
		$mail.Attachments.Add($Attachment2)     
		#>
		$smtp = New-Object System.Net.Mail.SmtpClient
		$smtp.Host = "smtp.gmail.com"
		$smtp.Port = 587
		$smtp.UseDefaultCredentials = $false
		$smtp.EnableSsl = $true
		$smtp.Credentials = New-Object System.Net.NetworkCredential($email, $password);
		$smtp.Send($mail)
	}
}