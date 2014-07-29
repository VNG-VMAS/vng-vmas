#!/usr/bin/env bash
# resolve links - "${BASH_SOURCE-$0}" may be a softlink
function print_usage(){
    echo "Usage: run <command> [<args>]"
    echo "Commands:"
    echo "Some commands take arguments. Pass no args or -h for usage."
    echo "  1. Checklog           Checklog and insert new server to mysql"
    echo "  2. 64to32             Convert Integer at 64bit to 32bit"
    echo "  3. ReadExcel          Parse excel2010 and insert to mysql"
    exit 1
}
if [ $# = 0 ]; then
  print_usage
  exit
fi
this="${BASH_SOURCE-$0}"
# convert relative path to absolute path
bin=`dirname "$this"`
script=`basename "$this"`
bin=`cd "$bin" > /dev/null;pwd`
this="$bin/$script"
cd $bin
case $1 in
3) java -cp $bin/*.jar com.vng.readexcel2010;;
2) java -cp $bin/*.jar com.vng.ConvertBigIntToInteger $2 $3;;
1) java -cp $bin/*.jar com.vng.CheckLog $2 $3 $4;;
esac
