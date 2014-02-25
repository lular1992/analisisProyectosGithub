package com.urjc.sonar;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;

public class EliminarRecursivamente {
	
	public boolean invokeDelete(String fileName, PrintWriter logBorradorFicheros){
	    File file = new File(fileName);
	    if (file.exists()) {
	        //check if the file is a directory
	        if (file.isDirectory()) {
	            if ((file.list()).length > 0) {
	                for(String s:file.list()){
	                	logBorradorFicheros.println("Archivo a eliminar "+fileName+"\\"+s+"\n");
	                    (new EliminarRecursivamente()).invokeDelete(fileName+"\\"+s,logBorradorFicheros);
	                }
	            }
	        }
	        boolean result = file.delete();
	        // test if delete of file is success or not
	        if (result) {
	        	logBorradorFicheros.println("Archivo " + fileName + " eliminado.\n");
	        } else {
	        	logBorradorFicheros.println("El archivo "+fileName+" no se ha podido borrar.\n");
	        }
	        return result;
	    } else {
	    	logBorradorFicheros.println("No se ha podido borrar el fichero "+fileName+" porque no existe.\n");
	        return false;
	    }
	}
	
	  public static boolean delete(String filePath, boolean recursive) {
	      File file = new File(filePath);
	      if (!file.exists()) {
	          return true;
	      }

	      if (!recursive || !file.isDirectory())
	          return file.delete();

	      String[] list = file.list();
	      for (int i = 0; i < list.length; i++) {
	    	  String archivoEliminar= filePath + File.separator + list[i];
	          if (!delete(archivoEliminar, true)){
		            System.out.println("El archivo "+archivoEliminar+" no se ha eliminado");
	              return false;
	          }else{	            
	        		  System.out.println("El archivo "+archivoEliminar+" se ha borrado");
	          }
	      }

	      return file.delete();
	  }
	  
		public static File[] buscarArchivos(String pathDirectorio,
				final String formato) {
			File dir = new File(pathDirectorio);

			return dir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					return filename.endsWith(formato);
				}
			});

		}

	

}
