package com.test.base64converter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RadioButton encodeChoice = findViewById(R.id.toB64);
        RadioButton decodeChoice = findViewById(R.id.fromB64);
        TextInputEditText plusSign = findViewById(R.id.plusSign);
        TextInputEditText slashSign = findViewById(R.id.slashSign);
        TextInputEditText equalSign = findViewById(R.id.equalSign);
        CheckBox notURLEncoding = findViewById(R.id.plusSlashEquals);
        TextInputEditText wordView = findViewById(R.id.stringToConvert);
        Button submit = findViewById(R.id.convertButton);
        TextView answerView = findViewById(R.id.answer);
        Button copy = findViewById(R.id.copy);

        final String[] plus = {"+"};
        final String[] slash = {"/"};
        final String[] equals = {"="};
        final boolean[] defaults = {true};
        notURLEncoding.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(!isChecked) {
                    plusSign.setEnabled(true);
                    slashSign.setEnabled(true);
                    equalSign.setEnabled(true);
                    defaults[0] = false;
                } else {
                    plus[0] = "+";
                    slash[0] = "/";
                    equals[0] = "=";
                    plusSign.setEnabled(false);
                    slashSign.setEnabled(false);
                    equalSign.setEnabled(false);
                    defaults[0] = true;
                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String word = String.valueOf(wordView.getText());
                String answer = "";
                if(defaults[0] == false) {
                    plus[0] = String.valueOf(plusSign.getText());
                    slash[0] = String.valueOf(slashSign.getText());
                    equals[0] = String.valueOf(equalSign.getText());
                    if(plus[0].length() == 0) {
                        plus[0] = "+";
                    }
                    if(slash[0].length() == 0) {
                        slash[0] = "/";
                    }
                    if(equals[0].length() == 0) {
                        equals[0] = "=";
                    }
                    if(plus[0].equals(slash[0]) || plus[0].equals(equals[0]) || slash[0].equals(equals[0])) {
                        answer = "Invalid. Do not set any of the (+, /, =) equivalents to be the same thing as one another.";
                    } else if(encodeChoice.isChecked()) {
                        answer = urlEncode(word, plus[0], slash[0], equals[0]);
                    } else if(decodeChoice.isChecked()) {
                        answer = urlDecode(word, plus[0], slash[0], equals[0]);
                    }
                } else {
                    if(encodeChoice.isChecked()) {
                        answer = encodeToBase64(word);
                    } else if(decodeChoice.isChecked()) {
                        answer = decodeBase64(word);
                    }
                }
                answerView.setText(answer);
            }
        });

        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String result = String.valueOf(answerView.getText());
                ClipboardManager clipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("", result);
                clipboard.setPrimaryClip(clip);
            }
        });
    }

    public String encodeToBase64(String word) {
        int evensets = (int) Math.floor(word.length() / 3);
        int stragglers = word.length() - (evensets * 3);
        String result = "";
        if (evensets > 0) {
            int k = 0;
            for (int i = 0; i < evensets; i++) {
                char one = word.charAt(k);
                char two = word.charAt(k + 1);
                char three = word.charAt(k + 2);
                char w = indexForEncoding((char)getFirstSixBits(one));
                char x = indexForEncoding((char)appendBits(getLastTwoBits(one), getFirstFourBits(two), 4));
                char y = indexForEncoding((char)appendBits(getLastFourBits(two), getFirstTwoBits(three), 2));
                char z = indexForEncoding((char)getLastSixBits(three));
                result += w;
                result += x;
                result += y;
                result += z;
                k += 3;
            }
        }
        if (stragglers == 1) {
            char one = word.charAt(word.length() - 1);
            char w = indexForEncoding((char)getFirstSixBits(one));
            char x = indexForEncoding((char)(getLastTwoBits(one) * 16));
            char y = '=';
            char z = '=';
            result += w;
            result += x;
            result += y;
            result += z;
        } else if (stragglers == 2) {
            char one = word.charAt(word.length() - 2);
            char two = word.charAt(word.length() - 1);
            char w = indexForEncoding((char)getFirstSixBits(one));
            char x = indexForEncoding((char)appendBits(getLastTwoBits(one), getFirstFourBits(two), 4));
            char y = indexForEncoding((char)(getLastFourBits(two) * 4));
            char z = '=';
            result += w;
            result += x;
            result += y;
            result += z;
        }
        return result;
    }

    public String decodeBase64(String word) {
        TextView answerView = findViewById(R.id.answer);
        if (word.length() % 4 != 0) {
            return "Invalid input. The word to be decoded must have a number of characters that is a multiple of 4.";
        }
        for(int p = 0; p < word.length(); p++) {
            if(indexForDecoding(word.charAt(p)) == 127 && word.charAt(p) != '=') {
                return "Invalid input. The word to be decoded may contain only capital letters, lowercase letters, numbers, and the following symbols: (+, /, =) or the equivalents you set.";
            } else if(word.charAt(p) == '=' && p < word.length() - 2) {
                return "Invalid input. The equal sign (=) cannot be in the middle of the string to decode. It should only be the last or 2nd to last character.";
            } else if(word.charAt(p) == '=' && p == word.length() - 2 && word.charAt(p + 1) != '=') {
                return "Invalid input. When decoding, an equal sign (=) cannot be followed by another character aside from another equal sign. There can only be up to 2 equal signs.";
            }
        }
        int evensets = 0;
        int stragglers = 0;
        String result = "";
        if (word.charAt(word.length() - 1) == '=') {
            evensets = (word.length() - 4) / 4;
            if (word.charAt(word.length() - 2) == '=') {
                stragglers = 1;
            } else {
                stragglers = 2;
            }
        } else {
            evensets = word.length() / 4;
        }
        if (evensets > 0) {
            int k = 0;
            for (int i = 0; i < evensets; i++) {
                char one = indexForDecoding(word.charAt(k));
                char two = indexForDecoding(word.charAt(k + 1));
                char three = indexForDecoding(word.charAt(k + 2));
                char four = indexForDecoding(word.charAt(k + 3));
                char w = (char)(appendBits(one, getFirstTwoBits((char)(two * 4)), 2));
                char x = (char)(appendBits(getLastFourBits(two), getFirstFourBits((char)(three * 4)), 4));
                char y = (char)(appendBits(getLastTwoBits(three), four, 6));
                result += w;
                result += x;
                result += y;
                k += 4;
            }
        }
        if (stragglers == 2) {
            char one = indexForDecoding(word.charAt(word.length() - 4));
            char two = indexForDecoding(word.charAt(word.length() - 3));
            char three = indexForDecoding(word.charAt(word.length() - 2));
            char w = (char)(appendBits(one, getFirstTwoBits((char)(two * 4)), 2));
            char x = (char)(appendBits(getLastFourBits(two), getFirstFourBits((char)(three * 4)), 4));
            result += w;
            result += x;
        } else if (stragglers == 1) {
            char one = indexForDecoding(word.charAt(word.length() - 4));
            char two = indexForDecoding(word.charAt(word.length() - 3));
            char w = (char)(appendBits(one, getFirstTwoBits((char)(two * 4)), 2));
            result += w;
        }

        return result;
    }
    public String urlEncode(String word, String plus, String slash, String equalSign) {
        String result = encodeToBase64(word);
        for(int i = 0; i < result.length(); i++) {
            if(result.charAt(i) == '+') {
                String remainder = "";
                if(i != result.length() - 1) {
                    remainder = result.substring(i + 1, result.length());
                }
                result = result.substring(0, i) + plus + remainder;
            }
        }
        result = result.replaceAll("/", slash);
        result = result.replaceAll("=", equalSign);
        return result;
    }
    public String urlDecode(String word, String plus, String slash, String equalSign) {
        for(int p = 0; p < word.length(); p++) {
            if((word.charAt(p) == '=' || word.charAt(p) == '+' || word.charAt(p) == '/') && word.charAt(p) != equalSign.charAt(0) && word.charAt(p) != slash.charAt(0) && word.charAt(p) != plus.charAt(0)) {
                return "Invalid input. The word to be decoded may contain only capital letters, lowercase letters, numbers, and the following symbols: (+, /, =) or the equivalents you set.";
            }
        }
        word = word.replaceAll(equalSign, "=");
        for(int i = 0; i < word.length(); i++) {
            if(word.substring(i, i + 1).equals(plus)) {
                String remainder = "";
                if(i != word.length() - 1) {
                    remainder = word.substring(i + 1, word.length());
                }
                word = word.substring(0, i) + "+" + remainder;
            }
        }
        word = word.replaceAll(slash, "/");
        return decodeBase64(word);
    }

    public String encodeHex(String word) {
        String hexstring = "";
        for (int i = 0; i < word.length(); i++) {
            if (word.charAt(i) < 16) {
                hexstring += '0';
            }
            hexstring += Integer.toHexString((int) word.charAt(i));
        }
        return hexstring;
    }

    public int appendBits(int a, int b, int bits2) {
        int c = (int) ((a * Math.pow(2, bits2)) + b);
        return c;
    }
    public int getFirstSixBits(char a) {
        int b = (int)a;
        for(int i = 0; i < 2; i++) {
            if(b % 2 != 0) {
                b -= 1;
            }
            b = b / 2;
        }
        return b;
    }

    public int getLastTwoBits(char a) {
        int b = (int)a;
        b -= (getFirstSixBits(a) * 4);
        return b;
    }

    public int getFirstTwoBits(char a) {
        int b = (int)a;
        for(int i = 0; i < 6; i++) {
            if(b % 2 != 0) {
                b -= 1;
            }
            b = b / 2;
        }
        return b;
    }

    public int getFirstFourBits(char a) {
        int b = (int)a;
        for(int i = 0; i < 4; i++) {
            if(b % 2 != 0) {
                b -= 1;
            }
            b = b / 2;
        }
        return b;
    }

    public int getLastFourBits(char a) {
        int b = (int)a;
        b -= (getFirstFourBits(a) * 16);
        return b;
    }

    public int getLastSixBits(char a) {
        int b = (int)a;
        b -= (getFirstTwoBits(a) * 64);
        return b;
    }

    public char indexForEncoding(char letter) {
        if (letter >= 0 && letter <= 25) {
            return (char) (letter + 65);
        }
        if (letter >= 26 && letter <= 51) {
            return (char) (letter + 71);
        }
        if (letter >= 52 && letter <= 61) {
            return (char) (letter - 4);
        }
        if (letter == 62) {
            return 43;
        }
        if (letter == 63) {
            return 47;
        }
        return letter;
    }

    public char indexForDecoding(char letter) {
        if (letter >= 65 && letter <= 90) {
            return (char) (letter - 65);
        }
        if (letter >= 97 && letter <= 122) {
            return (char) (letter - 71);
        }
        if (letter >= 48 && letter <= 57) {
            return (char) (letter + 4);
        }
        if (letter == 43) {
            return 62;
        }
        if (letter == 47) {
            return 63;
        }
        return 127;
    }
}