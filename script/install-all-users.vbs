If Not WScript.Arguments.Named.Exists("elevate") Then
  CreateObject("Shell.Application").ShellExecute WScript.FullName, """" & WScript.ScriptFullName & """ /elevate", "", "runas", 10
  WScript.Quit
End If

Set oShell = CreateObject("WScript.Shell")
Set oEnvSystem = oShell.Environment("SYSTEM")
Set oFS = CreateObject("Scripting.FileSystemObject")

Dim sBasePath, sJarFile
sBasePath = oFS.GetParentFolderName(oFS.GetParentFolderName(WScript.ScriptFullName))
sJarFile = sBasePath & "\jetbra-agent.jar"

If Not oFS.FileExists(sJarFile) Then
    MsgBox "jetbra-agent.jar not found", vbOKOnly Or vbCritical
    WScript.Quit -1
End If

MsgBox "It may take a few seconds to execute this script." & vbCrLf & vbCrLf & "Click 'OK' button and wait for the prompt of 'Done.' to pop up!"

Dim sEnvKey, sEnvVal, aJBProducts
aJBProducts = Array("idea", "clion", "phpstorm", "goland", "pycharm", "webstorm", "webide", "rider", "datagrip", "rubymine", "appcode", "dataspell", "gateway", "jetbrains_client", "jetbrainsclient", "studio", "devecostudio")

Set re = New RegExp
re.Global     = True
re.IgnoreCase = True
re.Pattern    = "^\-javaagent:.*[\/\\]jetbra\-agent\.jar.*"

Sub RemoveEnv(env)
    On Error Resume Next

    For Each sPrd in aJBProducts
        sEnvKey = UCase(sPrd) & "_VM_OPTIONS"
        sEnvVal = oShell.ExpandEnvironmentStrings("%" & sEnvKey & "%")
        If sEnvVal <> ("%" & sEnvKey & "%") Then
            env.Remove(sEnvKey)
        End If
        Next
End Sub

RemoveEnv oShell.Environment("USER")

Dim sVmOptionsFile
For Each sPrd in aJBProducts
    sEnvKey = UCase(sPrd) & "_VM_OPTIONS"
    sVmOptionsFile = sBasePath & "\vmoptions\" & sPrd & ".vmoptions"
    If oFS.FileExists(sVmOptionsFile) Then
        ProcessVmOptions sVmOptionsFile
        oEnvSystem(sEnvKey) = sVmOptionsFile
    End If
Next

Sub ProcessVmOptions(ByVal file)
    Dim sLine, sNewContent, bMatch
    Set oFile = oFS.OpenTextFile(file, 1, 0)

    sNewContent = ""
    Do Until oFile.AtEndOfStream
        sLine = oFile.ReadLine
        bMatch = re.Test(sLine)
        If Not bMatch Then
            sNewContent = sNewContent & sLine & vbLf
        End If
    Loop
    oFile.Close

    sNewContent = sNewContent & "-javaagent:" & sJarFile
    Set oFile = oFS.OpenTextFile(file, 2, 0)
    oFile.Write sNewContent
    oFile.Close
End Sub

MsgBox "Done."
