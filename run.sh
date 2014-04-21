#!/bin/bash


function new_java_file_exists() {

    java_regex='.*.java$'
    class_regex='.*.class$'

    java_flag='N'

    find . -type f \( -regex '.*.class' -or -regex '.*.java' \) -exec stat -f "%m %N" {} \; | sort -n -r | awk '{print $2}' | while read file
    do
        if [[ $file =~ $java_regex ]]
        then
            java_flag='Y'
        fi

        if [[ $file =~ $class_regex ]]
        then
            if test $java_flag = 'Y'
            then
                echo 'Y'
                return
            fi
        fi
    done
}

if test ! -d lib
then
    mkdir lib/
    mvn clean compile
    mvn process-sources -P dependency-cp
fi

echo 'Checking if there exists any changed java file'
if [[ `new_java_file_exists` == 'Y' ]]
then
    mvn clean compile
fi

if test $1 = 'init'
then
    echo 'Initializing keys.'
    cp=`find lib -regex ".*\.jar" -print0 | tr '\0' ':'`
    java -classpath ./target/classes/:$cp edu.utdallas.netsec.sfts.Initializer "$2" "$3"
fi

if test $1 = 'client'
then
    echo 'Running client'
    cp=`find lib -regex ".*\.jar" -print0 | tr '\0' ':'`
    java -classpath ./target/classes/:$cp edu.utdallas.netsec.sfts.client.Client "$2"
fi

if test $1 = 'as'
then
    echo 'Running Authentication Server'
    cp=`find lib -regex ".*\.jar" -print0 | tr '\0' ':'`
    java -classpath ./target/classes/:$cp edu.utdallas.netsec.sfts.as.AuthenticationServer "$2"
fi
