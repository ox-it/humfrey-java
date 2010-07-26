"""\
Uploads a document tree to a graph on a humfrey web service.

Example usage:

  python scripts/push_docs.py data/documents/ http://data.example.com/graph/documentation username password

You probably want the trailing slash (until this script is more resiliant).
"""

import sys, os, os.path, urlparse, rdflib
import StringIO

from util import upload

formats = {
    'n3': 'n3',
}


def main(dirname, graph_name, username, password):
    server_root = urlparse.urlparse(graph_name)
    server_root = urlparse.urlunparse(server_root[:2] + ('/','','',''))

    graph = rdflib.ConjunctiveGraph()

    for root, dirs, files in os.walk(dirname):
        for filename in files:
            filename = os.path.join(root, filename)
            uri = server_root + filename[len(dirname):]
            uri, format = uri.rsplit('.', 1)
            if uri.endswith('/index'):
                uri = uri[:-5]
            format = formats[format]

            graph.parse(open(filename, 'r'), uri, format)

    contents = StringIO.StringIO( graph.serialize(format='n3'))
    response = upload(contents, graph_name+'.n3', username, password)
    print response.code
    print response.read()


if __name__ == '__main__':
    if len(sys.argv) != 5:
        print __doc__
    else:
        main(*sys.argv[1:])
