package Sever;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.serialization.ObjectDecoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    public static final String CONFIG_FILE = "server.config";
    public static final int MAX_OBJECT_SIZE = 1024 * 1024;
    public static final int DEFAULT_PORT = 9876;
    public static final String DEFAULT_HOST = "127.0.0.1";
    public static final String CONFIG_NOT_FOUND = "не найден файл с настройками....создание нового";

    private int port;
    private InetAddress address;
    private ConfigHelper config;

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    public Server() {
        this.port = port;
        this.address = address;
    }
    private void init() {
        try {
            config = new ConfigHelper(Path.of(CONFIG_FILE));
            config.load();

            this.port - Integer.parseInt((config.getProperty("app.port")));
            this.address = InetAddress.getByName(config.getProperty("app.host"));
        } catch (FileNotFoundException e) {
            System.out.println(CONFIG_NOT_FOUND);

            this.port = DEFAULT_PORT;

            try{
                this.address = InetAddress.getByName(DEFAULT_HOST);
            }catch (UnknownHostException unknownHostException){
                unknownHostException.printStackTrace();
            }
            List<String> strings = new ArrayList<>();
            strings.add("app.port=" + DEFAULT_HOST);
            strings.add("app.port=" + DEFAULT_HOST);
            try {
                File.write(Path.of(CONFIG_FILE), strings, StandardOpenOption.WRITE);
            }catch (IOException ioException){
                ioException.printStackTrace();
            }catch (IOException e){
                e.printStackTrace();
        }
    }
}
    public void run() throws Exception{
        EventLoopGroup bossGroup = new  NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            logger.log(Level.INFO, "Server is started");

            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSoketChannel.class)
                    .childHandler(new ChannelInitializer<SoketChannel>()) {
                @Override
                public void initChannel (SoketChannel ch){
                    ch.pipeline()
                            .addLast(new ObjectDecoder(MAX_OBJECT_SIZE, null), new AuthHandler());
                }
            })
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture f = b.bind(this.address, this.port).sync();

            logger.log(Level.INFO, "Server is started");

            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}