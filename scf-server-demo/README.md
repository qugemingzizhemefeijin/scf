# SCF SERVER 端  DEMO

由于SCF在启动的时候，是加载jar包里面注解了ServiceContract和ServiceBehavior的类，所以只能在test下的启动。

测试入口 cg.zz.scf.test.server.main.Bootstrap

实际启动的时候，scf其实可以看做是一个容器，将src/test/resource拷贝到目录下，修改启动类等等，才能被启动。