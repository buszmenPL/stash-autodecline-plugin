#!/bin/bash

cd rep_1

echo "$1 pull requests will be created.."

echo "checkout master"
git checkout master

echo "pull --rebase"
git pull --rebase

for ((i=1; i<=$1; i++));
do
	sec=`date +%s`
	echo "$i: $sec"
	
	echo "checkout master"
	git checkout master
	
	echo "checkout -b branch-$sec"
	git checkout -b branch-$sec
	
	echo "updating content of file"
	echo "Content $sec" > ./basic_branching/file.txt
	
	echo "committing changes"
	git commit -am"Changes $sec"
	
	echo "git push"
	git push --set-upstream origin branch-$sec
	
	echo "creating pull request"
	stash pull-request master
	
	echo "-------------------------------"
done