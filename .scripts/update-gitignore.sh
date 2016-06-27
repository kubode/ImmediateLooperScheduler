#!/usr/bin/env bash
gibo -u
gibo osx windows jetbrains android > .gitignore
git rm -r --cached .
git add .
