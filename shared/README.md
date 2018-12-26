# Fuzzy Clock

## Explanation

Each localization can use their own rules of when to show which text. This means the following explanation might be wrong in  some specific languages. The following example will use English.

The app will show a different text at these minute marks:
- `:02 - :10` → Just past
- `:10 - :20` → Quarter past
- `:20 - :40` → Half past
- `:40 - :50` → Quarter past
- `:50 - :58` → Almost
- `:58 - :02` → Around

Hours are a bit trickier because the minute mark they change to the next one can be different in some languages. For example: in English we say `Half past ten`, while Dutch still already uses the next hour for expressing this (`Half elf`).

I try to use as much spoken language instead of translation the numbers (like some other apps do). Some examples include:
- `12AM / 00:00` → Midnight
- `12PM / 12:00` → Noon

## Adding translations

The code for creating the text was kept fairly amateurish so someone with basic programming knowledge can add their own translations. If you can't program but would still like to help send me a message and I'll figure something out (you'll probably get a list with all the text or something).

You will need to know the official abbreviation of the language you wish to add. Currently Android uses the [ISO639-2 specification](https://www.loc.gov/standards/iso639-2/php/code_list.php). If possible A `ISO639-1` (2nd column) code is preferred since older Android versions don't support v2.

### 1. Adding a clock translation

The files you need are located at [/shared/src/main/java/net/tuurlievens/fuzzyclock/](https://github.com/tuur29/fuzzyclock/tree/master/shared/src/main/java/net/tuurlievens/fuzzyclock).


1. Copying and refactor `FuzzyTextEnglish.kt` (filename & classname)
2. Register the new Class in the `create()` function of `FuzzyTextGenerator.kt` (line 7)
3. Replace the English text in the file you created. 
4. Edit the minute mark when the hour changes to the next one (around line 15)

### 2. Translating the app itself

While this isn't required, I and other users would greatly appreciate it. Doing this will also allow the user to manually select your language without changing their device settings.

Go to [/shared/src/main/res/](https://github.com/tuur29/fuzzyclock/tree/master/shared/src/main/res)

1. Add your language abbreviation to the `pref_list_language_values` list in `values/arrays.xml` (line 4)
2. Now translate your language and add them to the `pref_list_language_titles` list in each of the other language files at `values-XX/strings.xml`
3. Copy the `values/strings.xml` to a new director `values-XX/strings.xml` (XX is your language abbreviation).
4. Translate the new file you made.

> Note: You can simplify step **2.2** by only translating the value for the default language (English / en). Though keep in mind that only the English translation will be used in the settings regardless of the users locale.

> Note: Step **2.3** and **2.4** are also optional. If you don't do this, the English text is used instead.

### 3. Share your translation

Now you have a new translation it'd be a shame to keep it for yourself. The easiest way to share it is to create a [pull request](https://github.com/tuur29/fuzzyclock/pulls). I will then add it to the app and update it on the Google Play Store.

If for some reason you are unable or don't want to make a pull request, send me a message with the code you wrote and I will try to merge it myself.

## Development

> Note: When using Android Studio, edit your Run Configuration and set **Launch options** to `Nothing`.
