#!/bin/bash

git diff-index --quiet HEAD
if [ $? != 0 ]
then
    echo Dirty work tree. Aborting.
    exit 1
fi

echo "Release new version current version is $(grep "versionName \"" app/build.gradle | awk '{print $2}')"
read -p "Input new version name:" new_version_name
read -p "Retype new version name:" new_version_name_2
if [[ $new_version_name != $new_version_name_2 ]]
then
    echo Version number do not match. Aborting.
    exit 1
fi
new_version_code=$(($(git tag --merged | wc -l)+1))
echo New version name: $new_version_name
echo New version code: $new_version_code

sed -i "s/versionName \".*\"$/versionName \"$new_version_name\"/" app/build.gradle
sed -i "s/versionCode [0-9]*$/versionCode $new_version_code/" app/build.gradle

git add app/build.gradle
git commit -m "Bumping version to $new_version_name."
git tag -s $new_version_name -m "Version $new_version_name."
