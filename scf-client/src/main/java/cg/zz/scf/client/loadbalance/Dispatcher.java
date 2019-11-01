package cg.zz.scf.client.loadbalance;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import cg.zz.scf.client.communication.socket.ScoketPool;
import cg.zz.scf.client.configuration.ServiceConfig;
import cg.zz.scf.client.configuration.loadbalance.ServerProfile;
import cg.zz.scf.client.loadbalance.component.ServerChoose;
import cg.zz.scf.client.loadbalance.component.ServerState;
import cg.zz.scf.client.utility.logger.ILog;
import cg.zz.scf.client.utility.logger.LogFactory;

/**
 * 服务的调度器
 * @author chengang
 *
 */
public class Dispatcher {
	
	private static final ILog logger = LogFactory.getLogger(Dispatcher.class);
	
	/**
	 * 服务器列表
	 */
	private List<Server> ServerPool = new ArrayList<>();
	
	/**
	 * 累计值，用于GetServer中计算选中服务逻辑
	 */
	private AtomicInteger requestCount = new AtomicInteger(0);
	
	//应该使用ThreadLocalRandom对象
	final Random random = new Random();
	
	/**
	 * 构造Dispatcher
	 * @param config - 服务的配置
	 */
	public Dispatcher(ServiceConfig config) {
		logger.info("starting init servers");
		logger.debug("init connection begin:" + System.currentTimeMillis());
		for (ServerProfile ser : config.getServers()) {
			//权重必须>0才会被创建出来
			if (ser.getWeithtRate() > 0.0F) {
				Server s = new Server(ser);
				//如果服务状态为不可用状态，则不要加入到服务池
				if (s.getState() == ServerState.Disable) {
					continue;
				}
				
				//配置单个服务的连接池
				ScoketPool sp = new ScoketPool(s, config);
				s.setScoketpool(sp);
				
				//将服务加入到服务池中
				this.ServerPool.add(s);
			}
		}
		logger.debug("init connection end:" + System.currentTimeMillis());
		logger.info("init servers end");
	}
	
	/**
	 * 从server池中获取一个服务
	 * @return Server
	 */
	public Server GetServer() {
		if (this.ServerPool == null || this.ServerPool.size() == 0) {
			return null;
		}
		
		//下面逻辑默认是轮询
		int count = this.ServerPool.size();
		int start = this.requestCount.get() % count;
		if (this.requestCount.get() > 1000)
			this.requestCount.set(0);
		else {
			this.requestCount.getAndIncrement();
		}

		Server server = this.ServerPool.get(start);
		//如果服务状态不为正常状态，则随机获取服务
		if (server == null || server.getState() != ServerState.Normal) {
			start = this.random.nextInt(count);//随机到一个索引
			int next = getNext(count, start);//获取其下一个索引的值
			//此处逻辑就是先随机到1个服务器，然后从这个服务器开始，直至循环到他本身，如果有一个正常的服务，则返回。要么则没为空
			while (server == null || (server.getState() != ServerState.Normal && next != start)) {
				server = this.ServerPool.get(next);
				next = getNext(count, next);
			}
		}

		return server;
	}
	
	/**
	 * 获得start开始的下一个数字，如果start为最后一个，则返回0
	 * @param size - 总长度
	 * @param start - 开始位置
	 * @return int
	 */
	public final int getNext(int size, int start) {
		if (start == size - 1) {
			return 0;
		}
		return start + 1;
	}
	
	/**
	 * 根据服务名称获得Server
	 * @param name - 服务名称
	 * @return Server
	 */
	public Server GetServer(String name) {
		for (Server s : this.ServerPool) {
			if (s.getName().equalsIgnoreCase(name)) {
				return s;
			}
		}
		return null;
	}
	
	/**
	 * 获得所有的服务列表。这里应该返回一个不可变的List，防止被外部修改
	 * @return List<Server>
	 */
	public List<Server> GetAllServer() {
		return this.ServerPool;
	}
	
	/**
	 * 根据ServerChoose中维护的服务器列表获取服务对象，这个方法按照这一版本是不会被调用到的。
	 * @param sc - ServerChoose
	 * @return Server
	 */
	public Server GetServer(ServerChoose sc) {
		if (this.ServerPool == null || this.ServerPool.size() == 0) {
			return null;
		}
		Server result = null;
		int count = sc.getServiceCount();
		int start = this.requestCount.get() % count;
		if (this.requestCount.get() > 100)
			this.requestCount.set(0);
		else {
			this.requestCount.getAndIncrement();
		}
		
		//感觉这里面的计算逻辑有点问题。。。获取到正常的服务器，为啥不break出来？而是继续下一次循环
		for (int i = start; i < start + count; i++) {
			//计算出服务器索引，并且获取服务器对象
			int index = i % count;
			Server server = GetServer(sc.getServerName()[index]);
			int currUserCount = -1;
			//此处判断服务是否已经dead了，并且超过指定的时间，则将服务状态设置为正在测试中
			if (server.getState() == ServerState.Dead && System.currentTimeMillis() - server.getDeadTime() > server.getDeadTimeout()) {
				synchronized (this) {
					if (server.getState() == ServerState.Dead && System.currentTimeMillis() - server.getDeadTime() > server.getDeadTimeout()) {
						server.setState(ServerState.Testing);
						server.setDeadTime(0L);
						result = server;
						logger.warning("find server that need to test!host:" + server.getAddress());
						break;
					}
				}
			}
			
			//此处判断服务是否已经Reboot了，并且超过指定的时间，则将服务状态设置为正在测试中
			if (server.getState() == ServerState.Reboot && System.currentTimeMillis() - server.getDeadTime() > server.getDeadTimeout()) {
				synchronized (this) {
					if (server.getState() == ServerState.Reboot && System.currentTimeMillis() - server.getDeadTime() > server.getDeadTimeout()) {
						server.setState(ServerState.Testing);
						server.setDeadTime(0L);
						result = server;
						this.requestCount.getAndDecrement();
						logger.warning("find server that need to test!host:" + server.getAddress());
						break;
					}
				}
			}
			//如果当前服务的负载比上一个服务负载要大，或者服务的状态不是正常状态，则跳过本次循环
			if ((server.getCurrUserCount() >= currUserCount * server.getWeightRage() && currUserCount >= 0) || server.getState() != ServerState.Normal) continue;
			currUserCount = server.getCurrUserCount();
			result = server;
		}
		
		//如果上面未匹配成功 一个服务器，则走随机模式
		if (result == null) {
			if (this.ServerPool.size() - sc.getServiceCount() == 0) {
				result = this.ServerPool.get(new Random().nextInt(count));
			} else {
				//获取ServerPool中排除掉sc.getServerName集合的所有的服务
				int counts = this.requestCount.get() % (this.ServerPool.size() - sc.getServiceCount());
				result = GetServer(getNoName(sc.getServerName())[counts]);
			}
			
			logger.warning("Not get Specified server, This server is " + result.getState() + " DeadTime:" + result.getDeadTime() + " DeadTimeout" + result.getDeadTimeout());
		}
		
		return result;
	}
	
	/**
	 * 返回所有的服务名称，并且排除掉传入的指定服务名称的服务器
	 * @param serverName - 要排除的serverName
	 * @return String[]
	 */
	private String[] getNoName(String[] serverName) {
		String[] str = new String[this.ServerPool.size() - serverName.length];
		int count = 0;
		for (Server s : this.ServerPool) {
			for (String strName : serverName) {
				if (!s.getName().equals(strName)) {
					str[count++] = s.getName();
				}
			}
		}
		return str;
	}

}
