# edn-config

[![License](https://img.shields.io/github/license/chrisjdavies/edn-config.svg)](LICENSE)
[![Clojars Project](https://img.shields.io/clojars/v/chrisjd/edn-config.svg)](https://clojars.org/chrisjd/edn-config)
[![CircleCI](https://circleci.com/gh/chrisjdavies/edn-config.svg?style=svg)](https://circleci.com/gh/chrisjdavies/edn-config)

Captures the common pattern of loading configurations in EDN form
(with `clojure.edn/read-string`) and merging sensitive values in from
the environment.

The two main functions of this library are `edn-config.core/load-file`
and `edn-config.core/load-string`.  These functions essentially just
use `clojure.edn/read-string` to parse [EDN
data](https://github.com/edn-format/edn) from either a file or string,
respectively, but then also do some post-processing on the result when
the top-level form is a map:

- For every `:env` key in that map (and any nested child maps) that
  maps to another map, we call that an "env-map".
- The key-value pairs in the env-map are used to substitute
  environmental values into the parent map.  The environment is
  queried using [environ](https://github.com/weavejester/environ), so
  the lookup rules defined by that apply.

If the top-form isn't a map then the result of calling
`clojure.edn/read-string` on the data is simply returned.  This is
applying the principle of least surprise.

A typical example might be:

``` edn
{:username "test"
 :password "test"
 :env {:username :prod-username
       :password :prod-password}}
```

In the simple case, if there are no `:prod-username` or
`:prod-password` values in the environment, we just get this map back
as it is when it's loaded by edn-config.

If there is a value for `:prod-username` then `:username` is mapped to
that value in the top-level form.  The same goes for `:prod-password`
and `:password`.

This process is recursive, allowing configuration maps like the
following to be used:

``` edn
{:database {:username "test"
            :password "test"
            :host "localhost"
            :port 5432
            :env {:username :db-username
                  :password :db-password
                  :host :db-host
                  :port :db-port}}
 :web {:host "127.0.0.1"
       :port 8080
       :env {:host :web-host
             :port :web-port}}}
```

## Installation

Add the following to your `project.clj`:

```
[chrisjd/edn-config "0.1.0"]
```


## Documentation

- [API Docs](https://chrisjdavies.github.io/edn-config/)


## Usage

Load from a string (`edn-config.core/load-string`) or file
(`edn-config.core/load-file`):

``` clojure
user> (require '[edn-config.core :as cfg])
nil
user> (cfg/load-string "{:foo 123 :bar 456}")
{:foo 123, :bar 456}
user> (cfg/load-string "{:username \"foo\" :env {:username :user}}")
{:username "chris", :env {:username :user}}
user> (cfg/load-file "/tmp/config.edn")
{:language "en_GB.UTF-8", :env {:language :lang}}
```


## License

Copyright © 2018 Chris J-D

Distributed under the MIT License: https://opensource.org/licenses/MIT
