package com.guide.green.green_guide.Utilities;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.guide.green.green_guide.R;
import com.guide.green.green_guide.Utilities.Review.ReviewCategory;

public abstract class FormInput {
    public final Review.Key key;
    private FormInput mChild;
    private String mShowChildValue;
    private ReviewCategory mReviewCategory;
    private View mView;
    private String preHideValue;
    private View[] mGroupMembers;

    public FormInput(@NonNull View view) {
        this(null, null, view, null, null);
    }
    public FormInput(@Nullable Review.Key key, @Nullable ReviewCategory category,
                     @NonNull View view) {
        this(key, category, view, null, null);
    }
    public FormInput(@Nullable Review.Key key, @Nullable ReviewCategory category, @NonNull View view,
                       @Nullable FormInput child, @Nullable String showChildValue) {
        this(key, category, view, child, showChildValue, null);
    }
    public FormInput(@Nullable Review.Key key, @Nullable ReviewCategory category, @NonNull View view,
                     @Nullable FormInput child, @Nullable String showChildValue, @Nullable View[] inGroup) {
        this.key = key;
        mChild = child;
        mView = view;
        mShowChildValue = showChildValue;
        mReviewCategory = category;
        mGroupMembers = inGroup;
    }
    public void onValueChanged(String value) {
        if (mShowChildValue != null) {
            if (mShowChildValue.equals(value)) {
                showChild();
            } else {
                hideChild();
            }
        }
        if (mReviewCategory != null) {
            mReviewCategory.set(key, value);
        }
    }
    public void hide() {
        if (!isVisible()) { return; }
        mView.setVisibility(View.GONE);
        if (mReviewCategory != null) {
            preHideValue = mReviewCategory.get(key);
        }
        if (mGroupMembers != null) {
            for (View v : mGroupMembers) {
                v.setVisibility(View.GONE);
            }
        }
        onValueChanged("");
    }
    public void hideChild() {
        if (mChild != null && mChild.isVisible()) {
            mChild.hide();
        }
    }
    public void show() {
        if (isVisible()) { return; }
        mView.setVisibility(View.VISIBLE);
        onValueChanged(preHideValue);
        preHideValue = null;
        if (mGroupMembers != null) {
            for (View v : mGroupMembers) {
                v.setVisibility(View.VISIBLE);
            }
        }
    }
    public void showChild() {
        if (mChild != null && !mChild.isVisible()) {
            mChild.show();
        }
    }
    public boolean isVisible() {
        return mView.getVisibility() == View.VISIBLE;
    }

    public static void addAdapterToSpinner(Context context, Spinner spinner, int arrayId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                arrayId, R.layout.write_review_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public static class TextInput extends FormInput {
        public TextInput(@NonNull TextView textInput) {
            this(textInput, null, null);
        }
        public TextInput(@NonNull TextView textInput, @Nullable Review.Key key,
                         @Nullable ReviewCategory category) {
            super(key, category, textInput);
            String preSetValue = category.get(key);
            if (preSetValue != null) {
                textInput.setText(preSetValue);
            }
            textInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    /* Do Nothing */
                }
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    TextInput.this.onValueChanged(charSequence.toString());
                }
                @Override
                public void afterTextChanged(Editable editable) {
                    /* Do Nothing */
                }
            });
        }
    }

    public static class CheckBoxInput extends FormInput {
        public CheckBoxInput(@NonNull CheckBox checkBox, @NonNull Review.Key key,
                             @NonNull ReviewCategory category) {
            this(checkBox, key, category, null);
        }
        public CheckBoxInput(@NonNull CheckBox checkBox, @NonNull Review.Key key,
                             @NonNull ReviewCategory category, @Nullable FormInput child) {
            this(checkBox, key, category, child, null);
        }
        public CheckBoxInput(@NonNull CheckBox checkBox, @NonNull Review.Key key,
                             @NonNull ReviewCategory category, @Nullable FormInput child,
                             @NonNull View[] inGroup) {
            super(key, category, checkBox, child, child != null ? "on" : null, inGroup);
            String preSetValue = category.get(key);
            if (preSetValue != null) {
                checkBox.setChecked(preSetValue.equals(checkBox.getText().toString()));
            }
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    if (checked) {
                        onValueChanged("on");
                    } else {
                        onValueChanged(null);
                    }
                }
            });
        }
    }

    public static class YNRadioBtn extends RadioBtn {
        private String mYesValue;
        public YNRadioBtn(@NonNull RadioGroup radioGroup, @NonNull Review.Key key,
                        @NonNull ReviewCategory category) {
            this(radioGroup, key, category, null, null);
        }
        public YNRadioBtn(@NonNull RadioGroup radioGroup, @NonNull Review.Key key,
                        @NonNull ReviewCategory category, @Nullable FormInput child,
                        @Nullable String showChildValue) {
            this(radioGroup, key, category, child, showChildValue, null);
        }
        public YNRadioBtn(@NonNull RadioGroup radioGroup, @NonNull Review.Key key,
                        @NonNull ReviewCategory category, @Nullable FormInput child,
                        @Nullable String showChildValue, @Nullable View[] inGroup) {
            super(radioGroup, key, category, child, showChildValue == null ? null : "1", inGroup);
            mYesValue = showChildValue;
        }

        @Override
        public void onValueChanged(String value) {
            if (value != null && value.equals(mYesValue)) {
                super.onValueChanged("1");
            } else {
                super.onValueChanged("0");
            }
        }
    }

    public static class RadioBtn extends FormInput {
        public RadioBtn(@NonNull RadioGroup radioGroup, @NonNull Review.Key key,
                        @NonNull ReviewCategory category) {
            this(radioGroup, key, category, null, null);
        }
        public RadioBtn(@NonNull RadioGroup radioGroup, @NonNull Review.Key key,
                        @NonNull ReviewCategory category, @Nullable FormInput child,
                        @Nullable String showChildValue) {
            this(radioGroup, key, category, child, showChildValue, null);
        }
        public RadioBtn(@NonNull RadioGroup radioGroup, @NonNull Review.Key key,
                        @NonNull ReviewCategory category, @Nullable FormInput child,
                        @Nullable String showChildValue, @Nullable View[] inGroup) {
            super(key, category, radioGroup, child, showChildValue, inGroup);
            String preSetValue = category.get(key);
            if (preSetValue != null) {
                int childCount = radioGroup.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    RadioButton btn = (RadioButton) radioGroup.getChildAt(i);
                    if (btn.getText().toString().equals(preSetValue)) {
                        btn.setSelected(true);
                        break;
                    }
                }
            }
            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                    RadioButton radioBtn = radioGroup.findViewById(checkedId);
                    onValueChanged(radioBtn.getText().toString());
                }
            });
        }
    }

    public static class DropDown extends FormInput {
        private Spinner mDropDown;
        public DropDown(@NonNull Spinner dropDown, @NonNull Review.Key key,
                        @NonNull ReviewCategory category) {
            this(dropDown, key, category, null, null);
        }
        public DropDown(@NonNull Spinner dropDown, @NonNull Review.Key key,
                        @NonNull ReviewCategory category,
                        @Nullable FormInput child, @Nullable String showChildValue) {
            this(dropDown, key, category, child, showChildValue, null);
        }
        public DropDown(@NonNull Spinner dropDown, @NonNull Review.Key key,
                        @NonNull ReviewCategory category, @Nullable FormInput child,
                        @Nullable String showChildValue, @Nullable View[] inGroup) {
            super(key, category, dropDown, child, showChildValue, inGroup);
            mDropDown = dropDown;
            mDropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view,
                                           int pos, long id) {
                    String selectedValue = (String) adapterView.getItemAtPosition(pos);
                    onValueChanged(selectedValue);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    /* Do Nothing */
                }
            });
        }
    }
}