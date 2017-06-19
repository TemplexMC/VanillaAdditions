package io.github.trulyfree.va.command;

import io.github.trulyfree.va.command.commands.TabbableCommand;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@RequiredArgsConstructor
class ExternalCommandsHandler {

    private final CommandAdjuster commandAdjuster;

    public List<TabbableCommand> getExternalCommands() throws IllegalAccessException, IOException, InstantiationException {
        return getExternalCommands(new File(commandAdjuster.getPlugin().getDataFolder(), "commands.jar"));
    }

    private List<TabbableCommand> getExternalCommands(File file) throws IOException, IllegalAccessException, InstantiationException {
        List<Class<?>> commandClasses = getClasses(file);
        List<TabbableCommand> commands = new ArrayList<>();
        for (Class<?> clazz : commandClasses) {
            try {
                Object instance = clazz.newInstance();
                if (instance instanceof TabbableCommand) {
                    commands.add((TabbableCommand) instance);
                }
            } catch (IllegalAccessException | InstantiationException ignored) {
            }
        }
        return Collections.unmodifiableList(commands);
    }

    private List<Class<?>> getClasses(File file) throws IOException {
        if (!file.exists()) {
            commandAdjuster.getPlugin().getLogger().warning("External commands jar did not exist! (should be in plugin folder as 'commands.jar')");
            return Collections.emptyList();
        }

        List<Class<?>> list = new ArrayList<>();
        JarFile jarFile = new JarFile(file.getAbsolutePath());
        Enumeration<JarEntry> enumeration = jarFile.entries();

        URL[] urls = {new URL("jar:file:" + file.getAbsolutePath() + "!/")};
        URLClassLoader cl = URLClassLoader.newInstance(urls, this.getClass().getClassLoader());

        while (enumeration.hasMoreElements()) {
            try {
                JarEntry je = enumeration.nextElement();
                if (je.isDirectory() || !je.getName().endsWith(".class")) {
                    continue;
                }
                String className = je.getName().substring(0, je.getName().length() - 6);
                className = className.replace('/', '.');
                Class<?> c = cl.loadClass(className);
                list.add(c);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return Collections.unmodifiableList(list);
    }

}
