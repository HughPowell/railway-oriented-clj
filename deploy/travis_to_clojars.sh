#!/usr/bin/env bash

if ! [[ $(git log -1 --pretty=%B) =~ ^Version\ [0-9]+\.[0-9]+\.[0-9]+-SNAPSHOT$ ]] ; then
	echo "Deploying latest commit ..."
	git remote set-url origin https://$GITHUB_TOKEN@github.com/$GITHUB_ORG/railway-oriented-clj.git &&
	git branch --set-upstream-to origin/master master &&
	git config user.name "$RELEASERS_NAME" &&
	git config user.email "$RELEASERS_EMAIL" &&
	git config push.default simple &&
	git checkout master &&
	git pull origin master &&
	lein release :patch
else
	echo "Last commit created new snapshot, no deployment required."
fi
