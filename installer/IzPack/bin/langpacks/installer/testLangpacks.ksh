#!/bin/ksh

typeset -i count=0
langpacks=$(ls *.xml)

typeset help verbose
for opt; do
    case "$opt" in 
      -h*) help=true; shift; continue;;
      (-v*) verbose=true; shift; continue;;
    esac
done

if [ a$help = "atrue" ] ; then
    echo Shell scrip to verify entries in langpacks.
    echo This shell script writes the ids which are in eng.xml
    echo but not in the langpack with the given ISO3 to stdout.
    echo usage: $0 [-h] [-v] [ISO3]
    exit 0
fi
if [ $# -gt 0 ]; then
    langpacks=$1.xml
fi

for lp in $langpacks; do
    echo "Result for langpack $lp:"
    result=""
    count=0
    for i in `awk '{print $2}' eng.xml | grep 'id="'`; do
        MATCH=`grep "$i" $lp`;
        if [ "${MATCH}" = "" ]; then
            result[count]=$i
            count=count+1
        fi;
    done;
    if [ $count -eq 0 ] ; then
        echo "    All ids present"
    else
        echo "    $count IDs missing!"
        if [ a$verbose = "atrue" ] ; then
            i=2
            for line in ${result[*]} ; do
                echo "    $line"
                if [ $i -gt $count ] ; then
                    break
                fi
                i=$i+1
            done;
        fi
    fi
    
done;
