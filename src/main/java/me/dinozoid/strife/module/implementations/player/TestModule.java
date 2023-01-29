package me.dinozoid.strife.module.implementations.player;

import java.util.ArrayDeque;
import java.util.Deque;

import me.dinozoid.strife.alpine.listener.EventHandler;
import me.dinozoid.strife.alpine.listener.Listener;
import me.dinozoid.strife.event.implementations.network.PacketInboundEvent;
import me.dinozoid.strife.event.implementations.network.PacketOutboundEvent;
import me.dinozoid.strife.event.implementations.player.PlayerMotionEvent;
import me.dinozoid.strife.event.implementations.player.WorldLoadEvent;
import me.dinozoid.strife.module.Category;
import me.dinozoid.strife.module.Module;
import me.dinozoid.strife.module.ModuleInfo;
import me.dinozoid.strife.util.system.TimerUtil;
import net.minecraft.network.Packet;

@ModuleInfo(name = "Test", renderName = "Test", description = "A test module for developers.", category = Category.PLAYER)
public class TestModule extends Module {
	private final Deque<Packet<?>> packets = new ArrayDeque();
	private double x, y, z;
	private final TimerUtil timerOne = new TimerUtil();
	private final TimerUtil timerTwo = new TimerUtil();
	@EventHandler
	private final Listener<PacketOutboundEvent> outboundListener = new Listener<>(event -> {

	});
	
	@EventHandler
	private final Listener<PacketInboundEvent> inboundListener = new Listener<>(event -> {

	});
	
	@EventHandler
	private final Listener<WorldLoadEvent> loadListener = new Listener<>(event -> {

	});

	@EventHandler
	private final Listener<PlayerMotionEvent> updateListener = new Listener<>(event -> {

	});
	
}
