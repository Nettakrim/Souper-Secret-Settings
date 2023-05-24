# Souper Secret Settings
Re-adds the old super secret settings through the power of soup!

Simply drink a beetroot soup to activate a random souper secret setting, then drink milk or relog to clear it

You can use multiple souper secret settings at once using `/soup:stack`, and optifine/iris shaderpacks are compatible, so you can get some pretty cool looking combinations - personally i found that using [Epoch](https://modrinth.com/shader/epoch) as a base can look really cool.

Souper secret settings also expands on vanillas super secret settings by adding some custom ones, have fun eating soup!

![A collection of cool sights](https://cdn.modrinth.com/data/bzJkPbG1/images/e5320e13d8ab192c266c79dda2af46ec8414d77e.png)

## Commands
In the likely case you do not have access to beetroot soup, you can use a range of commands, all starting with `/soup:` to control the souper secret settings.

`/soup:set <name> (amount)` - Set to specific shader \<name>, with a suggested list of all the shaders, optionally specify an (amount) to stack at once

`/soup:random (amount)` - Randomise shader, same as drinking soup, optionally specify an (amount) to stack at once

`/soup:clear` - Clear shaders, same as drinking milk

`/soup:query` - Get the names of the currently active shaders

`/soup:stack add <name> (amount)` - Stacks the specified shader \<name> (amount) times

`/soup:stack random (amount)` - Adds a random (amount) shaders to the stack

`/soup:stack remove <name>` - Remove all occurrences of a specific shader in the stack

`/soup:stack undo (amount)` - Remove the top (amount) shaders in the stack

`/soup:toggle (stay)` - Toggle the shaders on and off to allow yourself to see clearly again without clearing the stack, by default it will automatically un-toggle if the stack gets changed

`/soup:recipe load|remove|save` - Manage presets for stacks of soup, so you can easily use them again later

## Custom Shaders
The list of available shaders, including the custom ones added by Souper Secret Settings, can be seen [Here](SuperSecretSettingsList.md)

Custom shaders can be added through resourcepacks, look [Here](ResourcepackGuide.md) for a guide on how to make a compatible resourcepack