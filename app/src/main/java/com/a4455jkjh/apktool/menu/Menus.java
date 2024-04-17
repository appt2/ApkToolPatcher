package com.a4455jkjh.apktool.menu;

import android.view.Menu;
import android.view.MenuItem;

import com.a4455jkjh.apktool.fragment.editor.EditorPagerAdapter;

import apk.tool.patcher.R;

public class Menus {
	public static void prepare(Menu menu, int idx) {
		for (MENU_ITEM item : MENU_ITEM.values())
			item.prepare(menu, idx);
	}
	public static void click(MenuItem item, int idx) {
		int id = item.getItemId();
		for (MENU_ITEM item1 : MENU_ITEM.values())
			if (item1.id == id)
				item1.click(item, idx);
	}
	private enum MENU_ITEM {
		SAVE(R.id.save){
			@Override
			void prepare(MenuItem item, int idx){
				item.setIcon(R.drawable.save);
				item.setEnabled(EditorPagerAdapter.INSTANCE.isEdited(idx));
			}
		},
		UNDO(R.id.undo){
			@Override
			void prepare(MenuItem item, int idx){
				item.setIcon(R.drawable.ic_undo);
				item.setEnabled(EditorPagerAdapter.INSTANCE.canUndo(idx));
			}
			@Override
			void click(MenuItem item, int idx){
				EditorPagerAdapter.INSTANCE.undo(idx);
			}
		},
		REDO(R.id.redo){
			@Override
			void prepare(MenuItem item, int idx){
				item.setIcon(R.drawable.ic_redo);
				item.setEnabled(EditorPagerAdapter.INSTANCE.canRedo(idx));
			}
			@Override
			void click(MenuItem item, int idx){
				EditorPagerAdapter.INSTANCE.redo(idx);
			}
		},
		FIND(R.id.find),
		GO_TO_LINE(R.id.go_to_line),
		EDIT_MODE(R.id.edit_mode){
			@Override
			void prepare(MenuItem item, int idx){
				if(EditorPagerAdapter.INSTANCE.isEditable(idx))
					item.setTitle(R.string.scan_mode);
				else
					item.setTitle(R.string.edit_mode);
			}
		},
		FORMAT(R.id.format){
			@Override
			void prepare(MenuItem item, int idx){
				item.setEnabled(EditorPagerAdapter.INSTANCE.
								canFormat(idx));
			}
			@Override
			void click(MenuItem item, int idx){
				EditorPagerAdapter.INSTANCE.format(idx);
			}
		};

		final int id;
		MENU_ITEM(int id)
		{
			this.id = id;
		}
		void prepare(Menu menu, int idx) {
			MenuItem item = menu.findItem(id);
			if(idx==-1)
				item.setVisible(false);
			else{
				item.setVisible(true);
				prepare(item,idx);
			}
		}

		void prepare(MenuItem item, int editor){
			// ignore
		}

		void click(MenuItem item, int idx){

		}
	}
}
