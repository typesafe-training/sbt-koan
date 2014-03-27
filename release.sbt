import sbtrelease.{ Version => ReleaseVersion, versionFormatError }

releaseSettings

ReleaseKeys.nextVersion := (v => ReleaseVersion(v) map (_.bumpMinor.asSnapshot.string) getOrElse versionFormatError)
