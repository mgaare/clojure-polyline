#!/usr/bin/env bash
cd $(realpath $(dirname $0))
# TODO: Source and load from common repository
if [ ! -f ./project.sh ]; then
	echo "Downloading bash helper utilities"
	curl -OL https://raw.githubusercontent.com/jesims/backpack/master/project.sh
fi
source ./project.sh
if [[ $? -ne 0 ]]; then
	exit 1
fi

## clean:
## Cleans up the compiled and generated sources
clean () {
	lein clean
	rm -rf .shadow-cljs/
}

## deps:
## Installs all required dependencies for Clojure and ClojureScript
deps () {
	echo_message 'Installing dependencies'
	lein deps
	abort_on_error
}

## unit-test:
## Runs the Clojure unit tests
unit-test () {
	clean
	lein test
	abort_on_error 'Clojure tests failed'
}

## unit-test-cljs:
## Runs the ClojureScript unit tests
unit-test-cljs () {
	clean
	lein node-test
	abort_on_error 'ClojureScript tests failed'
}

is-snapshot () {
	version=$(cat VERSION)
	[[ "$version" == *SNAPSHOT ]]
}

deploy () {
	if [[ -n "$CIRCLECI" ]];then
		lein deploy clojars &>/dev/null
		abort_on_error
	else
		lein deploy clojars
		abort_on_error
	fi
}

## snapshot:
## Pushes a snapshot to Clojars
snapshot () {
	if is-snapshot;then
		echo_message 'SNAPSHOT suffix already defined... Aborting'
		exit 1
	else
		version=$(cat VERSION)
		snapshot="$version-SNAPSHOT"
		echo ${snapshot} > VERSION
		echo_message "Snapshotting $snapshot"
		deploy
		echo "$version" > VERSION
	fi
}

## release:
## Pushes a release to Clojars
release () {
	version=$(cat VERSION)
	if ! is-snapshot;then
		version=$(cat VERSION)
		echo_message "Releasing $version"
		deploy
	else
		echo_message 'SNAPSHOT suffix already defined... Aborting'
		exit 1
	fi
}

if [[ "$#" -eq 0 ]] || [[ "$1" =~ ^(help|-h|--help)$ ]];then
	usage
	exit 1
elif [[ $(grep "^$1\ (" "$script_name") ]];then
	eval $@
else
	echo_error "Unknown function $1 ($script_name $@)"
	exit 1
fi
