package jason.wondermap.adapter;

import jason.wondermap.R;
import jason.wondermap.bean.Blog;
import jason.wondermap.bean.User;
import jason.wondermap.config.WMapConstants;
import jason.wondermap.fragment.BaseFragment;
import jason.wondermap.fragment.WMFragmentManager;
import jason.wondermap.manager.FootblogManager;
import jason.wondermap.sns.TencentShare;
import jason.wondermap.sns.TencentShareEntity;
import jason.wondermap.utils.ActivityUtil;
import jason.wondermap.utils.L;
import jason.wondermap.utils.T;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobPointer;
import cn.bmob.v3.datatype.BmobRelation;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * @author liuzhenhui
 * 
 */
@Deprecated
public class PersonCenterContentAdapter extends BaseContentAdapter<Blog> {

	public static final String TAG = "AIContentAdapter";
	public static final int SAVE_FAVOURITE = 2;

	public PersonCenterContentAdapter(Context context, List<Blog> list) {
		super(context, list);
	}

	@Override
	public View getConvertView(int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.ai_item, null);
			viewHolder.userName = (TextView) convertView
					.findViewById(R.id.user_name);
			viewHolder.userLogo = (ImageView) convertView
					.findViewById(R.id.user_logo);
			viewHolder.favMark = (ImageView) convertView
					.findViewById(R.id.item_action_fav);
			viewHolder.contentText = (TextView) convertView
					.findViewById(R.id.content_text);
			viewHolder.contentImage = (ImageView) convertView
					.findViewById(R.id.content_image);
			viewHolder.love = (TextView) convertView
					.findViewById(R.id.item_action_love);
			viewHolder.hate = (TextView) convertView
					.findViewById(R.id.item_action_hate);
			viewHolder.share = (TextView) convertView
					.findViewById(R.id.item_action_share);
			viewHolder.comment = (TextView) convertView
					.findViewById(R.id.item_action_comment);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		final Blog entity = dataList.get(position);
		L.i("user", entity.toString());
		User user = entity.getAuthor();
		if (user == null) {
			L.i("user", "USER IS NULL");
		}
		if (user.getAvatar() == null) {
			L.i("user", "USER avatar IS NULL");
		}
		String avatarUrl = null;
		if (user.getAvatar() != null) {
			avatarUrl = user.getAvatar();
		}
		ImageLoader.getInstance().displayImage(avatarUrl, viewHolder.userLogo,
				ActivityUtil.getOptions(R.drawable.default_head),
				new SimpleImageLoadingListener() {

					@Override
					public void onLoadingComplete(String imageUri, View view,
							Bitmap loadedImage) {
						// TODO Auto-generated method stub
						super.onLoadingComplete(imageUri, view, loadedImage);
					}

				});
		viewHolder.userLogo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				FootblogManager.getInstance().setCurrentBlog(entity);
				// User currentUser =
				// BmobUser.getCurrentUser(mContext,User.class);
				// if(currentUser != null){//已登录
				// Intent intent = new Intent();
				// intent.setClass(MyApplication.getInstance().getTopActivity(),
				// PersonalActivity.class);
				// mContext.startActivity(intent);
				// }else{//未登录
				// ActivityUtil.show(mContext, "请先登录。");
				// Intent intent = new Intent();
				// intent.setClass(MyApplication.getInstance().getTopActivity(),
				// RegisterAndLoginActivity.class);
				// MyApplication.getInstance().getTopActivity().startActivityForResult(intent,
				// Constant.GO_SETTINGS);
				// }
			}
		});
		viewHolder.userName.setText(entity.getAuthor().getUsername());
		viewHolder.contentText.setText(entity.getContent());
		if (null == entity.getContentfigureurl()) {
			viewHolder.contentImage.setVisibility(View.GONE);
		} else {
			viewHolder.contentImage.setVisibility(View.VISIBLE);
			ImageLoader
					.getInstance()
					.displayImage(
							entity.getContentfigureurl().getFileUrl(mContext) == null ? ""
									: entity.getContentfigureurl().getFileUrl(
											mContext), viewHolder.contentImage,
							ActivityUtil.getOptions(R.drawable.bg_pic_loading),
							new SimpleImageLoadingListener() {

								@Override
								public void onLoadingComplete(String imageUri,
										View view, Bitmap loadedImage) {
									// TODO Auto-generated method stub
									super.onLoadingComplete(imageUri, view,
											loadedImage);
									float[] cons = ActivityUtil
											.getBitmapConfiguration(
													loadedImage,
													viewHolder.contentImage,
													1.0f);
									RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
											(int) cons[0], (int) cons[1]);
									layoutParams.addRule(RelativeLayout.BELOW,
											R.id.content_text);
									viewHolder.contentImage
											.setLayoutParams(layoutParams);
								}

							});
		}
		viewHolder.love.setText(entity.getLove() + "");
		L.i("love", entity.getMyLove() + "..");
		if (entity.getMyLove()) {
			viewHolder.love.setTextColor(Color.parseColor("#D95555"));
		} else {
			viewHolder.love.setTextColor(Color.parseColor("#000000"));
		}
		viewHolder.hate.setText(entity.getHate() + "");
		viewHolder.love.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (entity.getMyLove()) {
					return;
				}
				entity.setLove(entity.getLove() + 1);
				viewHolder.love.setTextColor(Color.parseColor("#D95555"));
				viewHolder.love.setText(entity.getLove() + "");
				entity.setMyLove(true);
				entity.increment("love", 1);
				entity.update(mContext, new UpdateListener() {

					@Override
					public void onSuccess() {
						// TODO Auto-generated method stub
						L.i(TAG, "点赞成功~");
					}

					@Override
					public void onFailure(int arg0, String arg1) {
						// TODO Auto-generated method stub

					}
				});
			}
		});
		viewHolder.hate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				entity.setHate(entity.getHate() + 1);
				viewHolder.hate.setText(entity.getHate() + "");
				entity.increment("hate", 1);
				entity.update(mContext, new UpdateListener() {

					@Override
					public void onSuccess() {
						// TODO Auto-generated method stub
						T.showShort(mContext, "点踩成功~");
					}

					@Override
					public void onFailure(int arg0, String arg1) {
						// TODO Auto-generated method stub

					}
				});
			}
		});
		viewHolder.share.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// share to sociaty
				T.showShort(mContext, "分享给好友看哦~");
				final TencentShare tencentShare = new TencentShare(BaseFragment
						.getMainActivity(), getQQShareEntity(entity));
				tencentShare.shareToQQ();
			}
		});
		viewHolder.comment.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 评论
				FootblogManager.getInstance().setCurrentBlog(entity);
				// Intent intent = new Intent();
				// intent.setClass(MyApplication.getInstance().getTopActivity(),
				// CommentActivity.class);
				// mContext.startActivity(intent);
				BaseFragment.getWMFragmentManager().showFragment(
						WMFragmentManager.TYPE_FOOTBLOG_COMMENT);
			}
		});

		if (entity.getMyFav()) {
			viewHolder.favMark
					.setImageResource(R.drawable.ic_action_fav_choose);
		} else {
			viewHolder.favMark
					.setImageResource(R.drawable.ic_action_fav_normal);
		}
		viewHolder.favMark.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 收藏
				T.showShort(mContext, "收藏");
				onClickFav(v, entity);

			}
		});
		return convertView;
	}

	private TencentShareEntity getQQShareEntity(Blog qy) {
		String title = "这里好多美丽的风景";
		String comment = "来领略最美的风景吧";
		String img = null;
		if (qy.getContentfigureurl() != null) {
			img = qy.getContentfigureurl().getFileUrl(mContext);
		} else {
			img = "http://www.codenow.cn/appwebsite/website/yyquan/uploads/53af6851d5d72.png";
		}
		String summary = qy.getContent();

		String targetUrl = "http://huodianditu.bmob.cn";
		TencentShareEntity entity = new TencentShareEntity(title, img,
				targetUrl, summary, comment);
		return entity;
	}

	public static class ViewHolder {
		public ImageView userLogo;
		public TextView userName;
		public TextView contentText;
		public ImageView contentImage;

		public ImageView favMark;
		public TextView love;
		public TextView hate;
		public TextView share;
		public TextView comment;
	}

	private void onClickFav(View v, Blog qiangYu) {
		// TODO Auto-generated method stub
		User user = BmobUser.getCurrentUser(mContext, User.class);
		if (user != null && user.getSessionToken() != null) {
			BmobRelation favRelaton = new BmobRelation();

			qiangYu.setMyFav(!qiangYu.getMyFav());
			if (qiangYu.getMyFav()) {
				((ImageView) v)
						.setImageResource(R.drawable.ic_action_fav_choose);
				favRelaton.add(qiangYu);
				T.showShort(mContext, "收藏成功。");
			} else {
				((ImageView) v)
						.setImageResource(R.drawable.ic_action_fav_normal);
				favRelaton.remove(qiangYu);
				T.showShort(mContext, "取消收藏。");
			}

			user.setFavorite(favRelaton);
			user.update(mContext, new UpdateListener() {

				@Override
				public void onSuccess() {
					// TODO Auto-generated method stub
					L.i(TAG, "收藏成功。");
					// try get fav to see if fav success
					// getMyFavourite();
				}

				@Override
				public void onFailure(int arg0, String arg1) {
					// TODO Auto-generated method stub
					L.i(TAG, "收藏失败。请检查网络~");
					T.showShort(mContext, "收藏失败。请检查网络~" + arg0);
				}
			});
		} else {
			// 前往登录注册界面
			T.showShort(mContext, "收藏前请先登录。");
			// Intent intent = new Intent();
			// intent.setClass(mContext, RegisterAndLoginActivity.class);
			// MyApplication.getInstance().getTopActivity()
			// .startActivityForResult(intent, SAVE_FAVOURITE);
			BaseFragment.getWMFragmentManager().showFragment(WMFragmentManager.TYPE_LOGIN);
		}
	}

	private void getMyFavourite() {
		User user = BmobUser.getCurrentUser(mContext, User.class);
		if (user != null) {
			BmobQuery<Blog> query = new BmobQuery<Blog>();
			query.addWhereRelatedTo("favorite", new BmobPointer(user));
			query.include("user");
			query.order("createdAt");
			query.setLimit(WMapConstants.NUMBERS_PER_PAGE);
			query.findObjects(mContext, new FindListener<Blog>() {

				@Override
				public void onSuccess(List<Blog> data) {
					// TODO Auto-generated method stub
					L.i(TAG, "get fav success!" + data.size());
					T.showShort(mContext, "fav size:" + data.size());
				}

				@Override
				public void onError(int arg0, String arg1) {
					// TODO Auto-generated method stub
					T.showShort(mContext, "获取收藏失败。请检查网络~");
				}
			});
		} else {
			// 前往登录注册界面
			T.showShort(mContext, "获取收藏前请先登录。");
			// Intent intent = new Intent();
			// intent.setClass(mContext, RegisterAndLoginActivity.class);
			// MyApplication.getInstance().getTopActivity()
			// .startActivityForResult(intent, Constant.GET_FAVOURITE);
			BaseFragment.getWMFragmentManager().showFragment(WMFragmentManager.TYPE_LOGIN);
		}
	}
}