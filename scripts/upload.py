import urllib2, base64, sys

class AMR(urllib2.Request):
    def __init__(self, *args, **kwargs):
        self._method = kwargs.pop('method', None)
        urllib2.Request.__init__(self, *args, **kwargs)

    def get_method(self):
        return self._method or urllib2.Request.get_method(self)


def main(filename, graph, username, password, method='PUT'):
    data = open(filename).read() if method != 'DELETE' else None
    
    req = AMR(graph, data=data, method=method)
    req.headers['Authorization'] = 'Basic %s' % base64.b64encode('%s:%s' % (username, password))
    

    try:
        resp = urllib2.urlopen(req)
    except urllib2.HTTPError, e:
        resp = e
    print 'Result: %d' % resp.code
    print resp.headers
    print resp.read()

if __name__ == '__main__':
    main(*sys.argv[1:])

