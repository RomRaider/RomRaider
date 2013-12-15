@ECHO OFF
IF NOT EXIST "%HOMEPATH%\.RomRaider" (
	MKDIR "%HOMEPATH%\.RomRaider"
)
IF NOT EXIST "%HOMEPATH%\.RomRaider\settings.xml" (
	IF EXIST %1\settings.xml (
		MOVE /Y %1\settings.xml "%HOMEPATH%\.RomRaider\"
		MOVE /Y %1\profile_backup.xml "%HOMEPATH%\.RomRaider\"
	)
)
DEL /F %1\rr_system.log %1\romraider.log > NUL 2>&1
EXIT 0
