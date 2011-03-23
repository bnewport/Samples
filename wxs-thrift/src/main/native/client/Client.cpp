/*
 * Client.cpp
 *
 *  Created on: Feb 21, 2011
 *      Author: ibm
 */

#include "Client.h"
#include "../gen-cpp/WxsGatewayService.h"
#include <transport/TSocket.h>
#include <transport/TSocketPool.h>
#include <transport/TBufferTransports.h>
#include <protocol/TBinaryProtocol.h>

#include <boost/multi_index_container.hpp>
#include <boost/multi_index/tag.hpp>
#include <boost/multi_index/member.hpp>
#include <boost/multi_index/hashed_index.hpp>
#include <boost/multi_index/sequenced_index.hpp>
#include "lru_stl.h"
#include "lru_boost.h"

using namespace apache::thrift;
using namespace boost;

typedef boost::mpl::list<
  lru_boost<std::string,std::string>,
  lru_stl<std::string,std::string>
  > test_types;

Client::Client(const char *host)
{
//	  shared_ptr<transport::TSocket> socket = shared_ptr<transport::TSocket>(new transport::TSocket (host, 9100));

	lru_stl<const char *,const char *> *lru = new lru_stl<const char *,const char *>(1000);
	lru->insert("Hello", "Billy");
	const char * v_key = lru->get_keys("Hello");
	printf("%s\n", v_key);

	  shared_ptr<transport::TSocketPool> socket = shared_ptr<transport::TSocketPool>(new transport::TSocketPool (host, 9100));

	  std::string host2 = "localhost";
	  int port2 = 9101;
	  socket->addServer(host2, port2);

	  shared_ptr<transport::TFramedTransport> transport
	      = boost::shared_ptr<transport::TFramedTransport>
	        (new transport::TFramedTransport (socket));

	  shared_ptr<protocol::TBinaryProtocol> protocol = shared_ptr<protocol::TBinaryProtocol>
	      (new protocol::TBinaryProtocol (transport));

	  printf("Trying to connect\n");
	  transport -> open ();
	  WxsGatewayServiceClient *p = new WxsGatewayServiceClient (protocol);
	  std::string hostInUse = socket->getHost();
	  printf("Connected using %s : %d\n", hostInUse.c_str() , socket->getPort());

	std::string mapName = "Map";
	std::string key = "KEY";
	std::string value = "";
	for(int i = 0; i < 30; ++i)
		value.append("0123456789");
	p->put(mapName, key, value);

	int numIterations = 50000L;
	printf("Starting %d iterations\n", numIterations);
	clock_t start = clock();

	printf("Starting loop\n");
	for(int i = 0; i < numIterations; ++i)
	{
		std::string v;
		p->get(v, mapName, key);
		if(v.compare(value) != 0)
		{
			printf("Not the same\n");
		}
		if(i % 1000 == 0)
			printf(".");
	}
	printf("\n");
	clock_t end = clock();
	int gap = end - start;
	double timeInSeconds = gap / (double)CLOCKS_PER_SEC;
	printf("Done in %f seconds, raw is %d\n", timeInSeconds, gap);

	double ratePerSecond = numIterations / timeInSeconds;
	printf("gets per second is %f\n", ratePerSecond);
}

Client::~Client() {
	// TODO Auto-generated destructor stub
}

int main(int argc, char **argv)
{
	const char *host = "localhost";
	if(argc >= 2)
		host = argv[1];
	printf("Host is %s", host);
	Client *c = new Client(host);

}
