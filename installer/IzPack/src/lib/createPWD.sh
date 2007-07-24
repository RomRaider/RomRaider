# Script for generating Password class
# created on July 2005
# @author Fabrice Mirabile

#!/bin/sh

echo "Enter the name of the jar you want to create:"
read fname

if [ -n "$fname" ]; then 	# -n tests to see if the argument is non empty
	jar cf $fname ./com/izforge/izpack/sample/*.class
	echo "Success: jar $fname has been created"
else
	echo "No file name entered !"
fi
