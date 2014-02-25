package com.urjc.sonar;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.SearchRepository;
//import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;

public class ProbandoAPI {

	// PMD
	private static String pathEjecutablePMD = "F:\\Programas\\pmd-bin-5.1.0\\bin\\pmd.bat";
	private static String pathRulesetPMDComplejidad = "F:\\Programas\\pmd-bin-5.1.0\\rulesets\\rulesetTodaComplejidad.xml";
	private static String pathRulesetPMDLineasCodigo = "F:\\programas\\pmd-bin-5.1.0\\rulesets\\rulesetTodaLineasClase.xml";
	private static String pathInformeGenerado = "F:\\prueba\\informes\\";
	private static String formatoInforme = "csv";

	// GIT
	private static File pathRepositorioEnLocal;
	private static String raizProyecto = "F:/prueba";

	// GITHUB
	private static String pathListaRepositorios = "F:\\prueba\\resultadoBusqueda.txt";

	// LOGS
	private static PrintWriter logBorradoFicheros,log,logProyectosAnalizados,versiones;
	private static String pathLogBorrados = "F:\\prueba\\logs\\logBorradoFicheros.txt";
	private static String pathLog = "F:\\prueba\\logs\\errores.txt";
	private static String pathVersiones = "F:\\prueba\\informes\\0-ultimoPush.txt";
	private static String pathProyectosAnalizados = "F:\\prueba\\logs\\logProyectosAnalizados.txt";

	public static List<SearchRepository> searchRepo() throws IOException {

		RepositoryService service = new RepositoryService();

		Map<String, String> parametrosBusqueda = new HashMap<String, String>();

		parametrosBusqueda.put("language", "java");

		List<SearchRepository> resultadoBusqueda = service
				.searchRepositories(parametrosBusqueda);

		// La api de github solo muestra los 1000 primeros resultados
		// 20-feb-2014
		for (int i = 2; i < 11; i++) {
			List<SearchRepository> masResultados = service.searchRepositories(parametrosBusqueda, i);
			resultadoBusqueda.addAll(masResultados);
		}

		return resultadoBusqueda;
	}

	public static void mostrarProyectosBuscados(
			List<SearchRepository> resultadoBusqueda) {
		System.out.println("Num proyectos " + resultadoBusqueda.size());

		for (SearchRepository repo : resultadoBusqueda) {
			System.out.println("Nombre: " + repo.getName() + " Url: "
					+ repo.getUrl() + ".git");
		}
	}

	public static Process ejecutarComplejidadPMD(String pathProyecto,
			String nombreProyecto) {

		String comando = pathEjecutablePMD + " -dir " + pathProyecto + " -f "
				+ formatoInforme + " -R " + pathRulesetPMDComplejidad + ">  "
				+ pathInformeGenerado + nombreProyecto + "-complejidad-ciclo"
				+ "." + formatoInforme;

		Runtime runtime = Runtime.getRuntime();
		Process p1 = null;
		try {
			p1 = runtime.exec(comando);
		} catch (IOException e) {
			log.println("No se ha podido ejecutar la instrucción PMD complejidad ciclomática");
			log.println(e.getMessage());
		} catch (Exception e) {
			log.println("Error desconocido. Comando ejecutado: " + comando);
			log.println(e.getMessage());
		}

		return p1;

	}

	public static Process ejecutarLineasCodigoPMD(String pathProyecto,
			String nombreProyecto) {

		String comando = pathEjecutablePMD + " -dir " + pathProyecto + " -f "
				+ formatoInforme + " -R " + pathRulesetPMDLineasCodigo + ">  "
				+ pathInformeGenerado + nombreProyecto + "-lineas-codigo" + "."
				+ formatoInforme;

		Runtime runtime = Runtime.getRuntime();
		Process p1 = null;
		try {
			p1 = runtime.exec(comando);
		} catch (IOException e) {
			log.println("No se ha podido ejecutar la instrucción PMD líneas de código");
			log.println(e.getMessage());
		} catch (Exception e) {
			log.println("Error desconocido. Comando ejecutado: " + comando);
			log.println(e.getMessage());
		}

		return p1;

	}

	public static void borrarDirectorio(String path) {

		logBorradoFicheros.write("---Eliminando " + path + "\n\n");

		boolean eliminado = (new EliminarRecursivamente()).invokeDelete(path,
				logBorradoFicheros);

		logBorradoFicheros.write("---Terminado eliminar " + path + "\n\n\n");

		if (eliminado) {
			log.write("Eliminado todo " + path + "\n");
			logProyectosAnalizados.write("BORRADO\n");
		} else {
			log.write("No se ha podido eliminar todo el directorio " + path
					+ "\n");
		}
	}

	public static List<SearchRepository> recuperarLista() {
		InputStream archivoListaRepositorios;
		List<SearchRepository> listaRecuperada = null;

		try {
			archivoListaRepositorios = new FileInputStream(
					pathListaRepositorios);
			InputStream buffer = new BufferedInputStream(
					archivoListaRepositorios);
			ObjectInput input = new ObjectInputStream(buffer);
			// deserializar la lista
			listaRecuperada = (List<SearchRepository>) input.readObject();
			input.close();
		} catch (FileNotFoundException e) {
			log.println("No se ha encontrado el archivo que guarda la lista de repositorios.");
			log.println(e.getMessage());
		} catch (IOException e) {
			log.println("Fallo al crear Object Input Stream");
			log.println(e.getMessage());
		} catch (ClassNotFoundException e) {
			log.println("El tipo del fichero es distinto al tipo de la clase donde se quiere guardar");
			log.println(e.getMessage());
		}

		return listaRecuperada;
	}

	public static void guardarLista(List<SearchRepository> resultadoBusqueda)
			throws IOException {
		// serialize the List
		OutputStream file = new FileOutputStream(
				"D:\\prueba\\resultadoBusqueda.txt");
		OutputStream buffer = new BufferedOutputStream(file);
		ObjectOutput output = new ObjectOutputStream(buffer);
		output.writeObject(resultadoBusqueda);
		output.close();
	}

	public static int buscarIndexPorNombre(
			List<SearchRepository> listaRepositorios, String nombre) {

		int i = 0;
		boolean encontrado = false;
		while (!encontrado && i < listaRepositorios.size()) {
			encontrado = listaRepositorios.get(i).getName().equals(nombre);
			i++;
		}

		if (i >= listaRepositorios.size()) {
			return -1;
		} else {
			return i - 1;
		}

	}


	public static int clonarRepositorio(SearchRepository proyecto) {
		// Preparar una carpeta para el repositorio a clonar

		int exit = 0;
		String urlProyecto = proyecto.getUrl() + ".git";
		try {
			pathRepositorioEnLocal = File.createTempFile(proyecto.getName(),
					"", new File(raizProyecto));
			pathRepositorioEnLocal.delete();

			// Clonar
			log.println("Clonando desde " + urlProyecto + " a "
					+ pathRepositorioEnLocal + "\n");
			Git git = Git.cloneRepository().setURI(urlProyecto)
					.setDirectory(pathRepositorioEnLocal).call();

			git.close();

			logProyectosAnalizados.println("CLONADO\n");
		} catch (IOException e) {
			log.println("ERROR No se ha podido clonar el repositorio "
					+ proyecto.getName() + " " + urlProyecto);
			log.println(e.getMessage());
			logProyectosAnalizados.println("NO CLONADO");
			exit = 1;
		} catch (InvalidRemoteException e) {
			log.println("ERROR Url " + urlProyecto + " no valida.");
			log.println(e.getMessage());
			logProyectosAnalizados.println("NO CLONADO");
			exit = 1;
		} catch (TransportException e) {
			log.println("ERROR Transport exception.");
			log.println(e.getMessage());
			logProyectosAnalizados.println("NO CLONADO");
			exit = 1;
		} catch (GitAPIException e) {
			log.println("ERROR Api de git");
			log.println(e.getMessage());
			logProyectosAnalizados.println("NO CLONADO");
			exit = 1;
		} catch (Exception e) {
			log.println("ERROR desconocido");
			log.println("Url proyecto " + urlProyecto);
			log.println(e.getMessage());
			logProyectosAnalizados.println("NO CLONADO");
			exit = 1;
		}

		return exit;

	}

	public static void bigdataDesdeIndex(int indice, int fin,
			List<SearchRepository> resultadoBusqueda) {

		for (int i = indice; i < fin; i++) {
			
			SearchRepository proyecto = resultadoBusqueda.get(i);

			versiones.println(proyecto.getName() + "\n");
			versiones.println(proyecto.getPushedAt() + "\n");
			versiones.println(proyecto.hashCode() + "\n");

			log.println("-----" + proyecto.getName() + "-----\n\n");
			logProyectosAnalizados.println(i + " PROYECTO "
					+ proyecto.getName() + "\n\n");

			// Clonar el repositorio
			if (clonarRepositorio(proyecto) == 0) {

				// Pasarle pmd
				Process pmd = ejecutarComplejidadPMD(
						pathRepositorioEnLocal.getAbsolutePath(),
						pathRepositorioEnLocal.getName());

				Process lineasCodigo = ejecutarLineasCodigoPMD(
						pathRepositorioEnLocal.getAbsolutePath(),
						pathRepositorioEnLocal.getName());

				// Esperar a que termine
				try {
					final int exitStatus = pmd.waitFor();
					final int exitStatusLineas = lineasCodigo.waitFor();

					if (exitStatus == 0) {
						log.println("El informe de complejidad ciclomatica "
								+ pathRepositorioEnLocal.getName()
								+ " se ha creado con exito.\n\n");
						logProyectosAnalizados
								.println("ANALIZADA COMPLEJIDAD CICLOMATICA\n");

					} else {
						log.println("El informe de complejidad ciclomatica "
								+ pathRepositorioEnLocal.getName()
								+ " no se ha generado.\n\n");
						logProyectosAnalizados
								.println("NO ANALIZADA COMPLEJIDAD CICLOMATICA\n");
					}

					if (exitStatusLineas == 0) {
						log.println("El informe lineas de codigo de "
								+ pathRepositorioEnLocal.getName()
								+ " se ha creado con exito.\n\n");
						logProyectosAnalizados
								.println("ANALIZADAS LINEAS CODIGO\n");
					} else {
						log.println("El informe de lineas de codigo"
								+ pathRepositorioEnLocal.getName()
								+ " no se ha generado.\n\n");
						logProyectosAnalizados
								.println("NO ANALIZADA LINEAS DE CODIGO\n");
					}

					// Borrar el directorio
					borrarDirectorio(pathRepositorioEnLocal.getAbsolutePath());

					log.write("----- Análisis de " + pathRepositorioEnLocal
							+ " terminado. -----\n\n\n");
					logProyectosAnalizados.write("----\n\n");
				} catch (InterruptedException e) {
					log.println("ERROR proceso PMD interrumpido");
					log.println(e.getMessage());
				}
			}
		}
	}

	public static PrintWriter crearLog(String pathArchivo) {
		PrintWriter out = null;
		File archivoLog = new File(pathArchivo);

		// Si no existe el fichero, crearlo
		if (!archivoLog.exists()) {
			try {
				archivoLog.createNewFile();
			} catch (IOException e) {
				System.err.println("No se ha podido crear el fichero "
						+ archivoLog.getName() + ".");
				System.err.println(e.getMessage());
			}
		}

		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(
					pathArchivo, true)));
		} catch (IOException e) {
			System.err.println("No se ha podido crear el PrintWriter "
					+ archivoLog.getName() + ".");
			System.err.println(e.getMessage());
		}

		return out;
	}

	public static void main(String[] args) {

		try {

			// Crear los logs
			logBorradoFicheros = crearLog(pathLogBorrados);
			log = crearLog(pathLog);
			logProyectosAnalizados = crearLog(pathProyectosAnalizados);
			versiones = crearLog(pathVersiones);

			// List<SearchRepository> resultadoBusqueda = searchRepo();
			// guardarLista(resultadoBusqueda);

			List<SearchRepository> resultadoBusqueda = recuperarLista();

			bigdataDesdeIndex(0,resultadoBusqueda.size(), resultadoBusqueda);


		} finally {
			logBorradoFicheros.close();
			log.close();
			logProyectosAnalizados.close();
			versiones.close();
		}

	}

}