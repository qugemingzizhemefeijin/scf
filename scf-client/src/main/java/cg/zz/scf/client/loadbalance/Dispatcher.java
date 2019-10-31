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
	
	private List<Server> ServerPool = new ArrayList<>();
	
	private AtomicInteger requestCount = new AtomicInteger(0);
	
	final Random random = new Random();
	
	/**
	 * 构造Dispatcher
	 * @param config - 服务的配置
	 */
	public Dispatcher(ServiceConfig config) {
		logger.info("starting init servers");
		logger.debug("init connection begin:" + System.currentTimeMillis());
		for (ServerProfile ser : config.getServers()) {
			if (ser.getWeithtRate() > 0.0F) {
				Server s = new Server(ser);
				if (s.getState() == ServerState.Disable) continue;
				ScoketPool sp = new ScoketPool(s, config);
				s.setScoketpool(sp);
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
		
		int count = this.ServerPool.size();
		int start = this.requestCount.get() % count;
		if (this.requestCount.get() > 1000)
			this.requestCount.set(0);
		else {
			this.requestCount.getAndIncrement();
		}

		Server server = (Server) this.ServerPool.get(start);
		if (server == null || server.getState() != ServerState.Normal) {
			start = this.random.nextInt(count);
			long next = getNext(count, start);
			while ((server == null) || (server.getState() != ServerState.Normal && next != start)) {
				server = (Server) this.ServerPool.get((int) next);
				next = getNext(count, next);
			}
		}

		return server;
	}
	
	/**
	 * 获得start开始的下一个数字，如果start为最后一个，则返回0
	 * @param size - 总长度
	 * @param start - 开始位置
	 * @return long
	 */
	public final long getNext(int size, long start) {
		if (start == size - 1) {
			return 0L;
		}
		return start + 1L;
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
	 * 获得所有的服务列表
	 * @return List<Server>
	 */
	public List<Server> GetAllServer() {
		return this.ServerPool;
	}
	
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
		
		for (int i = start; i < start + count; i++) {
			int index = i % count;
			Server server = GetServer(sc.getServerName()[index]);
			int currUserCount = -1;
			//此处判断服务是否已经dead了
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
			
			//此处判断服务是否已经Reboot了
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
			
			if ((server.getCurrUserCount() >= currUserCount * server.getWeightRage() && currUserCount >= 0) || server.getState() != ServerState.Normal) continue;
			currUserCount = server.getCurrUserCount();
			result = server;
		}
		
		if (result == null) {
			if (this.ServerPool.size() - sc.getServiceCount() == 0) {
				result = (Server) this.ServerPool.get(new Random().nextInt(count));
			} else {
				int counts = this.requestCount.get() % (this.ServerPool.size() - sc.getServiceCount());
				result = GetServer(getNoName(sc.getServerName())[counts]);
			}
			
			logger.warning("Not get Specified server, This server is " + result.getState() + " DeadTime:" + result.getDeadTime() + " DeadTimeout" + result.getDeadTimeout());
		}
		
		return result;
	}
	
	/**
	 * 返回所有的服务名称，并且排除掉传入的指定的服务名称
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
