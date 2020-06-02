package l2s.gameserver.scripts;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.compiler.Compiler;
import l2s.commons.compiler.MemoryClassLoader;
import l2s.gameserver.Config;
import l2s.gameserver.handler.bypass.Bypass;
import l2s.gameserver.handler.bypass.BypassHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.scripts.annotation.OnScriptInit;
import l2s.gameserver.scripts.annotation.OnScriptLoad;

public class Scripts
{
	private static final Logger _log = LoggerFactory.getLogger(Scripts.class);

	private static final Scripts _instance = new Scripts();

	public static final Scripts getInstance()
	{
		return _instance;
	}

	public static final Map<Integer, List<ScriptClassAndMethod>> dialogAppends = new HashMap<Integer, List<ScriptClassAndMethod>>();
	public static final Map<String, ScriptClassAndMethod> onAction = new HashMap<String, ScriptClassAndMethod>();
	public static final Map<String, ScriptClassAndMethod> onActionShift = new HashMap<String, ScriptClassAndMethod>();

	private final Compiler compiler = new Compiler();
	private final Map<String, Class<?>> _classes = new TreeMap<String, Class<?>>();
	private final Map<Class<?>, Object> _instances = new ConcurrentHashMap<Class<?>, Object>();

	private Scripts()
	{
		load();
	}

	/**
	 * Вызывается при загрузке сервера. Загрузает все скрипты в data/scripts. Не инициирует объекты и обработчики.
	 *
	 * @return true, если загрузка прошла успешно
	 */
	private void load()
	{
		_log.info("Scripts: Loading...");

		List<Class<?>> classes = new ArrayList<Class<?>>();

		boolean result = false;

		File f = new File("scripts.jar");
		if(f.exists())
		{
			JarInputStream stream = null;
			try
			{
				stream = new JarInputStream(new FileInputStream(f));
				JarEntry entry = null;
				while((entry = stream.getNextJarEntry()) != null)
				{
					//Вложенные класс
					if(entry.getName().contains(ClassUtils.INNER_CLASS_SEPARATOR) || !entry.getName().endsWith(".class"))
						continue;

					String name = entry.getName().replace(".class", "").replace("/", ".");

						Class<?> clazz = Class.forName(name);
						if(Modifier.isAbstract(clazz.getModifiers()))
							continue;
						classes.add(clazz);
				}
				result = true;
			}
			catch (Exception e)
			{
				_log.error("Fail to load scripts.jar!", e);
				classes.clear();
			}
			finally
			{
				try
				{
					if(stream != null)
						stream.close();
				}
				catch(IOException ioe)
				{
					//
				}
			}
		}

		if(!result)
			result = load(classes, "");

		if(!result)
		{
			_log.error("Scripts: Failed loading scripts!");
			Runtime.getRuntime().exit(0);
			return;
		}

		_log.info("Scripts: Loaded " + classes.size() + " classes.");

		for(Class<?> clazz : classes)
		{
			_classes.put(clazz.getName(), clazz);

			try
			{
				Object o = getClassInstance(clazz);
				for(Method method : clazz.getMethods())
				{
					if(method.isAnnotationPresent(OnScriptLoad.class))
					{
						Class<?>[] par = method.getParameterTypes();
						if(par.length != 0)
						{
							_log.error("Wrong parameters for load method: " + method.getName() + ", class: " + clazz.getSimpleName());
							continue;
						}

						try
						{
							if(Modifier.isStatic(method.getModifiers()))
								method.invoke(clazz);
							else
							{
								if(o == null)
									o = clazz.newInstance();
								method.invoke(o);
							}
						}
						catch(Exception e)
						{
							_log.error("Exception: " + e, e);
						}
					}
				}
			}
			catch(Exception e)
			{
				_log.error("", e);
			}
		}
	}

	/**
	 * Вызывается при загрузке сервера. Инициализирует объекты и обработчики.
	 */
	public void init()
	{
		for(Class<?> clazz : _classes.values())
		{
			try
			{
				Object o = getClassInstance(clazz);
				if(ClassUtils.isAssignable(clazz, ScriptConfig.class))
				{
					try
					{
						if(o == null)
							o = clazz.newInstance();
						((ScriptConfig) o).load();
					}
					catch(Exception e)
					{
						_log.error("Scripts: Failed running " + clazz.getName() + ".load()", e);
					}
				}

				for(Method method : clazz.getMethods())
				{
					if(method.isAnnotationPresent(Bypass.class))
					{
						Class<?>[] par = method.getParameterTypes();
						if(par.length == 0 || par[0] != Player.class || par[1] != NpcInstance.class || par[2] != String[].class)
						{
							_log.error("Wrong parameters for bypass method: " + method.getName() + ", class: " + clazz.getSimpleName());
							continue;
						}

						Bypass an = method.getAnnotation(Bypass.class);
						if(Modifier.isStatic(method.getModifiers()))
							BypassHolder.getInstance().registerBypass(an.value(), clazz, method);
						else
						{
							if(o == null)
								o = clazz.newInstance();
							BypassHolder.getInstance().registerBypass(an.value(), o, method);
						}
					}
					else if(method.isAnnotationPresent(OnScriptInit.class))
					{
						Class<?>[] par = method.getParameterTypes();
						if(par.length != 0)
						{
							_log.error("Wrong parameters for init method: " + method.getName() + ", class: " + clazz.getSimpleName());
							continue;
						}

						try
						{
							if(Modifier.isStatic(method.getModifiers()))
								method.invoke(clazz);
							else
							{
								if(o == null)
									o = clazz.newInstance();
								method.invoke(o);
							}
						}
						catch(Exception e)
						{
							_log.error("Exception: " + e, e);
						}
					}
				}
			}
			catch(Exception e)
			{
				_log.error("", e);
			}
		}
	
		for(Class<?> clazz : _classes.values())
		{
			try
			{
				addHandlers(clazz);

				if(Config.DONTLOADQUEST)
				{
					if(ClassUtils.isAssignable(clazz, Quest.class))
						continue;
				}

				Object o = getClassInstance(clazz);
				if(ClassUtils.isAssignable(clazz, ScriptFile.class))
				{
					try
					{
						if(o == null)
							o = clazz.newInstance();
						((ScriptFile) o).onLoad();
					}
					catch(Exception e)
					{
						_log.error("Scripts: Failed running " + clazz.getName() + ".onLoad()", e);
					}
				}
			}
			catch(Exception e)
			{
				_log.error("", e);
			}
		}
	}

	/**
	 * Перезагрузить все скрипты в data/scripts
	 *
	 * @return true, если скрипты перезагружены успешно
	 */
	public boolean reload()
	{
		_log.info("Scripts: Reloading...");

		return reload("");
	}

	/**
	 * Перезагрузить все скрипты в data/scripts/target
	 *
	 * @param target путь до класса, или каталога со скриптами
	 * @return true, если скрипты перезагружены успешно
	 */
	public boolean reload(String target)
	{
		List<Class<?>> classes = new ArrayList<Class<?>>();

		Map<Class<?>, Object> oldInstances = new ConcurrentHashMap<Class<?>, Object>();
		if(load(classes, target))
		{
			oldInstances.putAll(_instances);
			_instances.clear();
			_log.info("Scripts: Reloaded " + classes.size() + " classes.");
		}
		else
		{
			_log.error("Scripts: Failed reloading script(s): " + target + "!");
			return false;
		}

		for(Class<?> clazz : classes)
		{
			Class<?> prevClazz = _classes.put(clazz.getName(), clazz);
			if(prevClazz != null)
			{
				Object o = oldInstances.get(prevClazz);
				if(ClassUtils.isAssignable(prevClazz, ScriptFile.class))
				{
					try
					{
						if(o == null)
							o = clazz.newInstance();
						((ScriptFile) o).onReload();
					}
					catch(Exception e)
					{
						_log.error("Scripts: Failed running " + prevClazz.getName() + ".onReload()", e);
					}
				}
				removeHandlers(prevClazz);
			}
		}

		init();

		return true;
	}

	/**
	 * Вызывается при завершении работы сервера
	 */
	public void shutdown()
	{
		for(Class<?> clazz : _classes.values())
		{
			if(ClassUtils.isAssignable(clazz, Quest.class))
				continue;

			Object o = getClassInstance(clazz);
			if(ClassUtils.isAssignable(clazz, ScriptFile.class))
			{
				try
				{
					if(o == null)
						o = clazz.newInstance();
					((ScriptFile) o).onShutdown();
				}
				catch(Exception e)
				{
					_log.error("Scripts: Failed running " + clazz.getName() + ".onShutdown()", e);
				}
			}
		}
	}

	/**
	 * Перезагрузить все скрипты в data/scripts/target
	 *
	 * @param classes, для загруженных скриптов
	 * @param target путь до класса, или каталога со скриптами
	 * @return true, если загрузка прошла успешно
	 */
	private boolean load(List<Class<?>> classes, String target)
	{
		Collection<File> scriptFiles = Collections.emptyList();

		File file = new File(Config.DATAPACK_ROOT, "data/scripts/" + target.replace(".", "/") + ".java");
		if(file.isFile())
		{
			scriptFiles = new ArrayList<File>(1);
			scriptFiles.add(file);
		}
		else
		{
			file = new File(Config.DATAPACK_ROOT, "data/scripts/" + target);
			if(file.isDirectory())
				scriptFiles = FileUtils.listFiles(file, FileFilterUtils.suffixFileFilter(".java"), FileFilterUtils.directoryFileFilter());
		}

		if(scriptFiles.isEmpty())
			return false;

		Class<?> clazz;
		boolean success;

		if(success = compiler.compile(scriptFiles))
		{
			MemoryClassLoader classLoader = compiler.getClassLoader();
			for(String name : classLoader.getLoadedClasses())
			{
				//Вложенные класс
				if(name.contains(ClassUtils.INNER_CLASS_SEPARATOR))
					continue;

				try
				{
					clazz = classLoader.loadClass(name);
					if(Modifier.isAbstract(clazz.getModifiers()))
						continue;
					classes.add(clazz);
				}
				catch(ClassNotFoundException e)
				{
					success = false;
					_log.error("Scripts: Can't load script class: " + name, e);
				}
			}
			classLoader.clear();
		}

		return success;
	}


	private void addHandlers(Class<?> clazz)
	{
		try
		{
			for(Method method : clazz.getMethods())
				if(method.getName().contains("DialogAppend_"))
				{
					Integer id = Integer.parseInt(method.getName().substring(13));
					List<ScriptClassAndMethod> handlers = dialogAppends.get(id);
					if(handlers == null)
					{
						handlers = new ArrayList<ScriptClassAndMethod>();
						dialogAppends.put(id, handlers);
					}
					handlers.add(new ScriptClassAndMethod(clazz.getName(), method.getName()));
				}
				else if(method.getName().contains("OnAction_"))
				{
					String name = method.getName().substring(9);
					onAction.put(name, new ScriptClassAndMethod(clazz.getName(), method.getName()));
				}
				else if(method.getName().contains("OnActionShift_"))
				{
					String name = method.getName().substring(14);
					onActionShift.put(name, new ScriptClassAndMethod(clazz.getName(), method.getName()));
				}
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
	}

	private void removeHandlers(Class<?> script)
	{
		try
		{
			for(List<ScriptClassAndMethod> entry : dialogAppends.values())
			{
				List<ScriptClassAndMethod> toRemove = new ArrayList<ScriptClassAndMethod>();
				for(ScriptClassAndMethod sc : entry)
					if(sc.className.equals(script.getName()))
						toRemove.add(sc);
				for(ScriptClassAndMethod sc : toRemove)
					entry.remove(sc);
			}

			List<String> toRemove = new ArrayList<String>();
			for(Map.Entry<String, ScriptClassAndMethod> entry : onAction.entrySet())
				if(entry.getValue().className.equals(script.getName()))
					toRemove.add(entry.getKey());
			for(String key : toRemove)
				onAction.remove(key);

			toRemove = new ArrayList<String>();
			for(Map.Entry<String, ScriptClassAndMethod> entry : onActionShift.entrySet())
				if(entry.getValue().className.equals(script.getName()))
					toRemove.add(entry.getKey());
			for(String key : toRemove)
				onActionShift.remove(key);
		}
		catch(Exception e)
		{
			_log.error("", e);
		}
	}

	public Object callScripts(String className, String methodName)
	{
		return callScripts(null, className, methodName, null, null);
	}

	public Object callScripts(String className, String methodName, Object[] args)
	{
		return callScripts(null, className, methodName, args, null);
	}

	public Object callScripts(String className, String methodName, Map<String, Object> variables)
	{
		return callScripts(null, className, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY, variables);
	}

	public Object callScripts(String className, String methodName, Object[] args, Map<String, Object> variables)
	{
		return callScripts(null, className, methodName, args, variables);
	}

	public Object callScripts(Player caller, String className, String methodName)
	{
		return callScripts(caller, className, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY, null);
	}

	public Object callScripts(Player caller, String className, String methodName, Object[] args)
	{
		return callScripts(caller, className, methodName, args, null);
	}

	public Object callScripts(Player caller, String className, String methodName, Map<String, Object> variables)
	{
		return callScripts(caller, className, methodName, ArrayUtils.EMPTY_OBJECT_ARRAY, variables);
	}

	public Object callScripts(Player caller, String className, String methodName, Object[] args, Map<String, Object> variables)
	{
		Class<?> clazz;

		clazz = _classes.get(className);
		if(clazz == null)
		{
			_log.error("Script class " + className + " not found!");
			return null;
		}

		Object o = getClassInstance(clazz);
		if(o == null)
		{
			try
			{
				o = clazz.newInstance();
			}
			catch(Exception e)
			{
				_log.error("Scripts: Failed creating instance of " + clazz.getName(), e);
				return null;
			}
		}

		if(variables != null && !variables.isEmpty())
		{
			for(Map.Entry<String, Object> param : variables.entrySet())
			{
				try
				{
							FieldUtils.writeField(o, param.getKey(), param.getValue());
				}
				catch(Exception e)
				{
					_log.error("Scripts: Failed setting fields for " + clazz.getName(), e);
				}
			}
		}

		if(caller != null)
		{
			try
			{
					Field field = null;
					if((field = FieldUtils.getField(clazz, "self")) != null)
						FieldUtils.writeField(field, o, caller.getRef());
			}
			catch(Exception e)
			{
				_log.error("Scripts: Failed setting field for " + clazz.getName(), e);
			}
		}

		Object ret = null;
		try
		{
			Class<?>[] parameterTypes = new Class<?>[args.length];
			for(int i = 0; i < args.length; i++)
				parameterTypes[i] = args[i] != null ? args[i].getClass() : null;

			ret = MethodUtils.invokeMethod(o, methodName, args, parameterTypes);
		}
		catch(NoSuchMethodException nsme)
		{
			_log.error("Scripts: No such method " + clazz.getName() + "." + methodName + "()!");
		}
		catch(InvocationTargetException ite)
		{
			_log.error("Scripts: Error while calling " + clazz.getName() + "." + methodName + "()", ite.getTargetException());
		}
		catch(Exception e)
		{
			_log.error("Scripts: Failed calling " + clazz.getName() + "." + methodName + "()", e);
		}

		return ret;
	}

	public boolean containsClass(String className)
	{
		return _classes.containsKey(className);
	}

	public Map<String, Class<?>> getClasses()
	{
		return _classes;
	}

	public Object getClassInstance(Class<?> clazz)
	{
		return _instances.get(clazz);
	}

	public Object getClassInstance(String className)
	{
		Class<?> clazz = _classes.get(className);
		if(clazz != null)
			return getClassInstance(clazz);
		return null;
	}

	public static class ScriptClassAndMethod
	{
		public final String className;
		public final String methodName;

		public ScriptClassAndMethod(String className, String methodName)
		{
			this.className = className;
			this.methodName = methodName;
		}
	}
}