package jason.wondermap.manager;

import jason.wondermap.MainActivity;
import jason.wondermap.R;
import jason.wondermap.WonderMapApplication;
import jason.wondermap.bean.ChatMessage;
import jason.wondermap.bean.HelloMessage;
import jason.wondermap.bean.User;
import jason.wondermap.dao.UserDB;
import jason.wondermap.interfacer.OnBaiduPushNewFriendListener;
import jason.wondermap.interfacer.OnBaiduPushNewMessageListener;
import jason.wondermap.interfacer.OnNetChangeListener;
import jason.wondermap.task.SendMsgAsyncTask;
import jason.wondermap.utils.ConvertUtil;
import jason.wondermap.utils.L;
import jason.wondermap.utils.StaticConstant;
import jason.wondermap.utils.T;
import jason.wondermap.utils.TimeUtil;

import java.util.ArrayList;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.text.TextUtils;

public class PushMsgManager {
	/**
	 * 处理推送过来的消息
	 * 
	 * @param msg
	 */
	public void handleMsg(HelloMessage msg) {
		parseMessage(msg);
	}

	/**
	 * 对收到的消息进行分析，1，新人消息 2，自动回复消息 3，普通消息
	 */
	private void parseMessage(HelloMessage msg) {
		L.d("parseMessage into");
		String userId = msg.getUserId();
		// 自己的消息
		if (userId.equals(WonderMapApplication.getInstance().getSpUtil()
				.getUserId())) {
			L.d("自己的消息，返回");
			return;
		}
		// 新人加入或者自动回复，都要在地图上显示
		if (checkHasNewFriend(msg) || checkAutoResponse(msg))
			return;
		// 普通消息
		UserDB userDB = WonderMapApplication.getInstance().getUserDB();
		User user = userDB.selectInfo(userId);
		// 漏网之鱼,用户好友两层逻辑，用户存放到list，好友存放到数据库
		if (user == null) {
			user = new User(userId, msg.getChannelId(), msg.getNickname(),
					msg.getHeadIcon(), msg.getLat(), msg.getLng(), 0);
			// userDB.addUser(user);//暂时不做存储好友
			// 通知监听的面板,新人加入
			for (OnBaiduPushNewFriendListener listener : friendListeners)
				listener.onNewFriend(user);
		}
		if (msgListeners.size() > 0) {// 有监听的时候，传递下去
			for (int i = 0; i < msgListeners.size(); i++)
				msgListeners.get(i).onNewMessage(msg);
		} else
		// 当前没有任何监听，即处理后台状态
		{
			// 将新来的消息进行存储
			ChatMessage chatMessage = new ChatMessage(msg.getMessage(), true,
					userId, msg.getHeadIcon(), msg.getNickname(), false,
					TimeUtil.getTime(msg.getTimeSamp()));
			WonderMapApplication.getInstance().getMessageDB()
					.add(userId, chatMessage);
			// showNotify(msg);
		}
	}

	/**
	 * ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝检测是否是新人加入，代表的是老人＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
	 */
	private boolean checkHasNewFriend(HelloMessage msg) {
		L.d("checkHasNewFriend into");
		String userId = msg.getUserId();
		String hello = msg.getHello();
		if (!TextUtils.isEmpty(hello)) {
			boolean hasUserIdAdd = WMapUserManager.getInstance().containsUser(
					userId);
			// 添加过则更新位置
			if (hasUserIdAdd) {
				WMapUserManager.getInstance().updateUser(msg);
				L.d(msg.getNickname() + "已经添加过，更新位置");
			}
			// 没添加则添加
			else {
				User u = ConvertUtil.HelloMsgToUser(msg);
				WMapUserManager.getInstance().addUser(u);
				// WonderMapApplication.getInstance().getUserDB().addUser(u);
				// 存入或更新好友，暂时不做好友功能
				T.showShort(WonderMapApplication.getInstance(), u.getNick()
						+ "加入");
			}
			HelloMessage message = new HelloMessage(System.currentTimeMillis(),
					"");
			message.setWorld(StaticConstant.SayWorld);
			new SendMsgAsyncTask(WonderMapApplication.getInstance().getGson()
					.toJson(message), userId).send();
			// // 通知监听的面板，新人加入
			// for (OnBaiduPushNewFriendListener listener : friendListeners)
			// listener.onNewFriend(u);
			return true;
		}
		return false;
	}

	/**
	 * ＝＝＝＝＝＝＝＝＝＝＝＝＝＝检测是否是自动回复，代表的是新人＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
	 * 
	 * @param msg
	 */
	private boolean checkAutoResponse(HelloMessage msg) {
		String world = msg.getWorld();
		String userId = msg.getUserId();
		if (!TextUtils.isEmpty(world)) {
			User u = new User(userId, msg.getChannelId(), msg.getNickname(),
					msg.getHeadIcon(), msg.getLat(), msg.getLng(), 0);
			WMapUserManager.getInstance().addUser(u);
			// WonderMapApplication.getInstance().getUserDB().addUser(u);//
			// 存入或更新好友
			T.showShort(WonderMapApplication.getInstance(), "收到" + u.getNick()
					+ "回复");
			// 通知监听的面板
			for (OnBaiduPushNewFriendListener listener : friendListeners)
				listener.onNewFriend(u);
			// 添加好友位置
			return true;
		}
		return false;
	}
	//＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝暂时用不到＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
	//＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝暂时用不到＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
	// ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝暂时用不到＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
	/**
	 * 新消息的监听
	 */
	public static ArrayList<OnBaiduPushNewMessageListener> msgListeners = new ArrayList<OnBaiduPushNewMessageListener>();
	/**
	 * 新用户加入的监听
	 */
	public static ArrayList<OnBaiduPushNewFriendListener> friendListeners = new ArrayList<OnBaiduPushNewFriendListener>();
	/**
	 * 网络的监听
	 */
	public static ArrayList<OnNetChangeListener> netListeners = new ArrayList<OnNetChangeListener>();
	public static final int NOTIFY_ID = 0x000;
	public static int mNewNum = 0;// 通知栏新消息条目，我只是用了一个全局变量，

	private void showNotify(HelloMessage message) {
		mNewNum++;
		// 更新通知栏
		WonderMapApplication application = WonderMapApplication.getInstance();

		int icon = R.drawable.copyright;
		CharSequence tickerText = message.getNickname() + ":"
				+ message.getMessage();
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, tickerText, when);
		notification.flags = Notification.FLAG_NO_CLEAR;
		// 设置默认声音
		// notification.defaults |= Notification.DEFAULT_SOUND;
		// 设定震动(需加VIBRATE权限)
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notification.contentView = null;

		Intent intent = new Intent(application, MainActivity.class);
		// 当点击通知时，我们让原有的Activity销毁，重新实例化一个
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(application, 0,
				intent, 0);
		notification.setLatestEventInfo(WonderMapApplication.getInstance(),
				application.getSpUtil().getUserNick() + " (" + mNewNum
						+ "条新消息)", tickerText, contentIntent);
		application.getNotificationManager().notify(NOTIFY_ID, notification);// 通知一下才会生效哦
	}

	// ＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝模式化代码＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝＝
	private PushMsgManager() {

	}

	private static PushMsgManager instance = null;

	public static PushMsgManager getInstance() {
		if (instance == null) {
			instance = new PushMsgManager();
		}
		return instance;
	}

}