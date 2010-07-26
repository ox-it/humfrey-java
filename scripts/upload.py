import urllib2, base64, sys

from util import upload

if __name__ == '__main__':
    args = sys.argv[1:]
    args[0] = open(args[0], 'r')

    response = upload(*args)

    print response.code
    print response.read()

