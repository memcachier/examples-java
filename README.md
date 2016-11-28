# MemCachier Java Example App

This is an example Java Jetty app that uses
[MemCachier](http://www.memcachier.com) to cache Fibonacci digits.

You can view a working version of this app
[here](http://memcachier-examples-java.herokuapp.com). Running this
app on your local machine in development will work as well, although
you'll need to install a local memcached server. MemCachier is
currently only available with various cloud providers.

## Deploy to Heroku

You can deploy this app yourself to Heroku to play with.

[![Deploy](https://www.herokucdn.com/deploy/button.png)](https://heroku.com/deploy)

## Client

This example uses
[Spymemcached](https://code.google.com/p/spymemcached/) as the client
for connecting to memcachier.

Another very good choice is
[XMemcached](https://github.com/killme2008/xmemcached).

## Building

The Apache Maven build system is used. Simply execute:

```sh
$ mvn package
```

## Get involved!

We are happy to receive bug reports, fixes, documentation enhancements,
and other improvements.

Please report bugs via the
[github issue tracker](http://github.com/memcachier/examples-java/issues).

Master [git repository](http://github.com/memcachier/examples-java):

* `git clone git://github.com/memcachier/examples-java.git`

## Licensing

This library is BSD-licensed.

