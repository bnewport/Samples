namespace java com.devwebsphere.wxsthrift.gen
namespace py com.devwebsphere.wxsthrift.gen

service WxsGatewayService
{
	binary get(1:string mapName, 2:binary key)
	void remove(1:string mapName, 2:binary key) 
	void put(1:string mapName, 2:binary key, 3:binary value)
	
	list<binary> getAll(1:string mapName, 2:list<binary> keyList)
	void removeAll(1:string mapName, 2:list<binary> keyList)
	void putAll(1:string mapName, 2:list<binary> keys, 3:list<binary> values)
}
