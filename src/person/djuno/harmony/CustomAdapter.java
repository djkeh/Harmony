package person.djuno.harmony;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomAdapter extends ArrayAdapter<PresetData> {

	private final ArrayList<PresetData> items;
	private PresetData m;
	private final Context context;
	private final AssetManager asset;

	public CustomAdapter(Context context, int textViewResourceId, ArrayList<PresetData> items, AssetManager asset) {
		super(context, textViewResourceId, items);
		this.items = items;
		this.context = context;
		this.asset = asset;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		View v = convertView;
		TextView tvName;
		if (v == null) {

			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			v = vi.inflate(R.layout.item, null);

		}
		m = items.get(position);

		if (m != null) {
			tvName = ViewHolder.get(v, R.id.tv_name);
			tvName.setText(m.getName());
			Typeface face = Typeface.createFromAsset(asset, "fonts/NanumGothicExtraBold.ttf");
			tvName.setTypeface(face);
		}

		return v;

	}

	@Override
	public PresetData getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

}