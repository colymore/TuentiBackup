package com.colymore.tuenti;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.wb.swt.SWTResourceManager;
public class Main {

	protected Shell shell;
	private final FormToolkit formToolkit = new FormToolkit(
			Display.getDefault());
	private Text etDirectorio;
	private Text etEmail;
	private Text etPassword;

	private String email;
	private String password;
	private String cookieSesion;
	private StringBuilder body;
	private String csrf;
	private String enlaceFotoSiguiente;

	private File directorioFotos = null;

	private HttpURLConnection Conexion;

	private List<String> cookies;

	/**
	 * @wbp.parser.entryPoint
	 */
	public static void main(String[] args) {
		try {
			Main window = new Main();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		creaInterfaz();
		shell.open();
		shell.layout();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	protected void creaInterfaz() {

		shell = new Shell();
		shell.setSize(450, 300);
		shell.setText("Tuenti Backup");

		Button btnSeleccionarDirectorio = new Button(shell, SWT.NONE);
		// Click boton seleccionar directorio
		btnSeleccionarDirectorio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				// Abro el cuadro de dialogo
				DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
				directorioFotos = new File(dialog.open());

				etDirectorio.setText(dialog.open().toString());

			}
		});

		btnSeleccionarDirectorio.setFont(SWTResourceManager.getFont(
				"Droid Sans Mono", 9, SWT.NORMAL));
		btnSeleccionarDirectorio.setBounds(10, 33, 185, 25);
		formToolkit.adapt(btnSeleccionarDirectorio, true, true);
		btnSeleccionarDirectorio.setText("Seleccionar Directorio");

		etDirectorio = new Text(shell, SWT.BORDER | SWT.READ_ONLY);
		etDirectorio.setBounds(201, 34, 175, 21);
		formToolkit.adapt(etDirectorio, true, true);

		etEmail = new Text(shell, SWT.BORDER);
		etEmail.setBounds(201, 93, 175, 21);
		formToolkit.adapt(etEmail, true, true);

		Label lblEmailDeTuenti = new Label(shell, SWT.SHADOW_NONE | SWT.CENTER);
		lblEmailDeTuenti.setFont(SWTResourceManager.getFont("Droid Sans Mono",
				9, SWT.NORMAL));
		lblEmailDeTuenti.setAlignment(SWT.CENTER);
		lblEmailDeTuenti.setBounds(10, 93, 185, 21);
		formToolkit.adapt(lblEmailDeTuenti, true, true);
		lblEmailDeTuenti.setText("Email de Tuenti:");

		Label lblNewLabel = new Label(shell, SWT.NONE);
		lblNewLabel.setAlignment(SWT.CENTER);
		lblNewLabel.setBounds(10, 153, 185, 25);
		formToolkit.adapt(lblNewLabel, true, true);
		lblNewLabel.setText("Contrase\u00F1a:");

		etPassword = new Text(shell, SWT.BORDER | SWT.PASSWORD);
		etPassword.setBounds(201, 153, 175, 21);
		formToolkit.adapt(etPassword, true, true);

		Button btnDescargarFotos = new Button(shell, SWT.NONE);
		btnDescargarFotos.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Click descargar Fotos

				try {
					email = etEmail.getText();
					if (!email.trim().equals("")) {
						try {
							new InternetAddress(email).validate();
						} catch (AddressException ex) {
							MessageBox mensajeValidacionEmail = new MessageBox(
									shell);
							mensajeValidacionEmail.setText("Error");
							mensajeValidacionEmail
									.setMessage("Debes introducir un mail valido");
							mensajeValidacionEmail.open();
						}
					}
				} catch (NullPointerException nullException) {
					nullException.printStackTrace();
					MessageBox mensajeValidacionEmail = new MessageBox(shell);
					mensajeValidacionEmail.setText("Error");
					mensajeValidacionEmail
							.setMessage("Debes introducir un mail valido");
					System.out.println("Error null:"
							+ nullException.getMessage());
				}

				try {
					password = etPassword.getText();
					if (password.trim().equals("")) {
						MessageBox mensajeValidacionPass = new MessageBox(shell);
						mensajeValidacionPass.setText("Error");
						mensajeValidacionPass
								.setMessage("Debes introducir una contraseña");

					}
				} catch (NullPointerException nullException) {
					MessageBox mensajeValidacionPass = new MessageBox(shell);

					mensajeValidacionPass.setText("Error");
					mensajeValidacionPass
							.setMessage("Debes introducir una contraseña");
					nullException.printStackTrace();
					System.out.println("Error null:"
							+ nullException.getMessage());

				}

				descargarFotos(email, password);
			}
		});
		btnDescargarFotos.setBounds(159, 208, 102, 25);
		formToolkit.adapt(btnDescargarFotos, true, true);
		btnDescargarFotos.setText("Descargar Fotos");

	}

	private void descargarFotos(String email, String password) {

		// Primero recogo el pid de la cookie inicial y el csrf
		try {
			getWeb(new URL("http://m.tuenti.com/?m=Login"));
			cookies.get(0);
			cookieSesion = "cookiename=1; " + "pid="
					+ getCadenaEnString(cookies.get(0), "pid=", ";");

			System.out.println("Cookie inicial: " + cookieSesion);

			String cadenaInicioBusqueda = "name=\"csrf\" value=\"";
			String cadenaFinalBusqueda = "\"/>";

			csrf = getCadenaEnString(body.toString(), cadenaInicioBusqueda,
					cadenaFinalBusqueda);

		} catch (MalformedURLException e) {
			System.out.println("Error url" + e.getMessage());
			e.printStackTrace();
		}

		// Me logueo
		List<NameValuePair> postArgs = new ArrayList<NameValuePair>(2);
		postArgs.add(new BasicNameValuePair("csrf", csrf));
		postArgs.add(new BasicNameValuePair("tuentiemailaddress", email));
		postArgs.add(new BasicNameValuePair("password", password));
		postArgs.add(new BasicNameValuePair("remember", "1"));

		try {
			postWeb(new URL("http://m.tuenti.com/?m=Login&f=process_login"),
					cookieSesion, postArgs);

		} catch (MalformedURLException e) {

			e.printStackTrace();
		}

		// Voy al perfil
		try {
			// Busco el link de las fotos
			getWeb(new URL("http://m.tuenti.com/?m=Profile&func=my_profile"),
					cookieSesion);
			String cadenaInicioBusqueda = "<div class=\"h\">Fotos</div><a id=\"photos\"></a><div class=\"item\"><div> <small> <a href=\"";
			String cadenaFinalBusqueda = "\">";
			String urlAlbum = "http://m.tuenti.com/"
					+ getCadenaEnString(body.toString(), cadenaInicioBusqueda,
							cadenaFinalBusqueda);

			// Entro a la galeria y busco fotos en las que salgo
			getWeb(new URL(urlAlbum), cookieSesion);
			cadenaInicioBusqueda = "</h1><div class=\"item\"><a class=\"thumb\" href=\"";
			cadenaFinalBusqueda = "\">";
			String primeraFoto = null;
			try {
				primeraFoto = eliminarAmpDeUrL(URLDecoder.decode(
						"http://m.tuenti.com/"
								+ getCadenaEnString(body.toString(),
										cadenaInicioBusqueda,
										cadenaFinalBusqueda), "UTF-8"));
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}

			// Recogo el contenido del album, numero de fotos, enlace de foto 2
			// y sucesivas
			getWeb(new URL(primeraFoto), cookieSesion);
			int numeroDeFotos = Integer.valueOf(getCadenaEnString(
					body.toString(), "1 de ", ")"));
			cadenaInicioBusqueda = ") <a href=\"";
			cadenaFinalBusqueda = "\">Siguiente";
			try {
				enlaceFotoSiguiente = eliminarAmpDeUrL(URLDecoder.decode(
						"http://m.tuenti.com/"
								+ getCadenaEnString(body.toString(),
										cadenaInicioBusqueda,
										cadenaFinalBusqueda), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			// Creo el directorio en el que se guardaran las fotos
			if (new File(directorioFotos.toString() + "/FotosTuenti/" + email)
					.exists()) {
				System.out.println("Directorio creado");
			} else {
				if (!new File(directorioFotos.toString() + "/FotosTuenti/"
						+ email).mkdirs()) {
					JOptionPane
							.showMessageDialog(null,
									"Se ha producido un error creando la carpeta de destino");
				}

			}

			// Busco enlace de la primera foto y la descargo y genero el Image
			getWeb(new URL(primeraFoto), cookieSesion);
			cadenaInicioBusqueda = "\"thumb fullSize\"><img src=\"";
			cadenaFinalBusqueda = "\"";
			String jpgPrimeraFoto = null;
			try {
				jpgPrimeraFoto = eliminarAmpDeUrL(URLDecoder.decode(
						getCadenaEnString(body.toString(),
								cadenaInicioBusqueda, cadenaFinalBusqueda),
						"UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			getImagen(new URL(jpgPrimeraFoto), 1);
			System.out.println("Primera imagen descargada");

			// Primera imagen descargada, descargo el resto
			for (int i = 2; i <= numeroDeFotos; i++) {

				getWeb(new URL(enlaceFotoSiguiente), cookieSesion);
				cadenaInicioBusqueda = ") <a href=\"";
				cadenaFinalBusqueda = "\">Siguiente";
				try {
					enlaceFotoSiguiente = eliminarAmpDeUrL(URLDecoder.decode(
							"http://m.tuenti.com/"
									+ getCadenaEnString(body.toString(),
											cadenaInicioBusqueda,
											cadenaFinalBusqueda), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				cadenaInicioBusqueda = "\"thumb fullSize\"><img src=\"";
				cadenaFinalBusqueda = "\"";
				String jpgFotoSiguiente = null;
				try {
					jpgFotoSiguiente = eliminarAmpDeUrL(URLDecoder.decode(
							getCadenaEnString(body.toString(),
									cadenaInicioBusqueda, cadenaFinalBusqueda),
							"UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				getImagen(new URL(jpgFotoSiguiente), i);
				System.out.println("Imagen" + i + "descargada");

			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Metodo para recibir por get una url
	 * 
	 * @param url
	 *            A la cual hacemos la peticion
	 * 
	 * @return true si correcto false si no
	 */
	private Boolean getWeb(URL url) {

		try {
			Conexion = (HttpURLConnection) url.openConnection();
			Conexion.setDoInput(true);
			Conexion.setConnectTimeout(40000);
			Conexion.setDoOutput(true);
			Conexion.setUseCaches(false);
			Conexion.setDefaultUseCaches(false);
			Conexion.setRequestProperty("Accept", "*/*");
			Conexion.setRequestProperty(
					"User-agent",
					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.7; rv:12.0) Gecko/20100101 Firefox/12.0");
			InputStream in = Conexion.getInputStream();
			String encoding = Conexion.getContentEncoding();
			encoding = encoding == null ? "UTF-8" : encoding;

			body = inputStreamaBody(in);
			cookies = Conexion.getHeaderFields().get("Set-Cookie");

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Metodo para recibir por get una url pasandole una cookie
	 * 
	 * @param url
	 *            A la cual hacemos la peticion
	 * @param cookie
	 *            Cookie que se le quiere enviar
	 * @return true si correcto false si no
	 */
	private Boolean getWeb(URL url, String cookie) {

		try {
			Conexion = (HttpURLConnection) url.openConnection();
			Conexion.setDoInput(true);
			Conexion.setConnectTimeout(40000);
			Conexion.setDoOutput(true);
			Conexion.setUseCaches(false);
			Conexion.setDefaultUseCaches(false);
			Conexion.setRequestProperty("Accept", "*/*");
			Conexion.setRequestProperty(
					"User-agent",
					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.7; rv:12.0) Gecko/20100101 Firefox/12.0");
			Conexion.setRequestProperty("Cookie", cookie);
			InputStream in = Conexion.getInputStream();
			String encoding = Conexion.getContentEncoding();
			encoding = encoding == null ? "UTF-8" : encoding;

			body = inputStreamaBody(in);
			cookies = Conexion.getHeaderFields().get("Set-Cookie");

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Metodo para generar una peticion http Post
	 * 
	 * @param url
	 *            A la cual hacemos la peticion
	 * @param cookie
	 *            Cookie que se le quiere enviar
	 * @param postData
	 *            Parametros HTTP que enviaremos
	 * @return true si correcto false si no
	 */
	private Boolean postWeb(URL url, String cookie, List<NameValuePair> postData) {

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(
				"http://m.tuenti.com/?m=Login&f=process_login");
		httppost.addHeader("Cookie", cookie);
		System.out.println("Cookie en PrePost " + cookie);
		try {

			httppost.setEntity(new UrlEncodedFormEntity(postData));
			HttpResponse response = httpclient.execute(httppost);
			Reader reader = new InputStreamReader(response.getEntity()
					.getContent());
			StringBuffer sb = new StringBuffer();
			{
				int read;
				char[] cbuf = new char[1024];
				while ((read = reader.read(cbuf)) != -1)
					sb.append(cbuf, 0, read);
			}

			List<org.apache.http.Header> listaArrays = Arrays.asList(response
					.getHeaders("Set-Cookie"));

			for (int i = 0; i < listaArrays.size(); i++) {
				cookieSesion += listaArrays.get(i).getValue() + ";";
			}

			cookieSesion += " screen=1920-1080-1920-1040-1-20.74";
			System.out.println("Cookie en PostPost " + cookieSesion);

		} catch (ClientProtocolException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	/**
	 * Metodo para descargar una foto pasandole la URL
	 * 
	 * @param url
	 *            de la foto
	 * @return true si es correcto false si no
	 */
	private Boolean getImagen(URL url, int i) {

		try {
			InputStream is = url.openStream();
			OutputStream os = new FileOutputStream(directorioFotos
					+ "/FotosTuenti/" + email + "\\" + i + ".jpg");
			byte[] b = new byte[2048 * 4];
			int length;

			while ((length = is.read(b)) != -1) {
				os.write(b, 0, length);
			}

			is.close();
			os.close();

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error descargando la imagen");
			return false;
		}

		return true;
	}

	/**
	 * Metodo para encontrar un string dentro de otro a partir de string inicial
	 * y string final
	 * 
	 * @param totalCadena
	 *            Cadena en la que buscar el String
	 * @param inicioCadena
	 *            String incial para la busqueda
	 * @param finalCadena
	 *            String final de la busqueda
	 * @return String final con la cadena
	 */
	private String getCadenaEnString(String totalCadena, String inicioCadena,
			String finalCadena) {

		int posInicio = totalCadena.indexOf(inicioCadena);
		if (posInicio == -1)
			return "";
		int posFinal = totalCadena.indexOf(finalCadena, posInicio
				+ inicioCadena.length());
		totalCadena.substring(posFinal);
		String cadenaEncontrada = totalCadena.substring(posInicio
				+ inicioCadena.length(), posFinal);
		return cadenaEncontrada;

	}

	/**
	 * Metodo para generar un string a partir de un list<String>
	 * 
	 * @param listaString
	 *            Lista que contiene los strings
	 * @param caracter
	 *            String que separa cada elemento
	 * @return String que contiene los elementos separados por el caracter
	 */
	public static String implodeLista(List<Header> listaString, String caracter) {

		String salida = "";

		if (listaString.size() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append(listaString.get(0));

			for (int i = 1; i < listaString.size(); i++) {
				sb.append(caracter);
				sb.append(listaString.get(i));
			}

			salida = sb.toString();
		}

		return salida;
	}

	/**
	 * Metodo para limpiar una URl de caracteres extraños
	 * 
	 * @param url
	 *            que queremos limpiar
	 * @return String url limpia
	 */
	public String eliminarAmpDeUrL(String url) {

		url = StringEscapeUtils.unescapeHtml4(url).replaceAll("[^\\x20-\\x7e]",
				"");

		return url;
	}

	/**
	 * Metodo para convertir un InputStream en un StringBuilder Util para Bodys
	 * muy largos
	 * 
	 * @param is
	 *            InputStream que contiene el texto
	 * @return body StringBuilder que contiene el body
	 * @throws IOException
	 */
	public static StringBuilder inputStreamaBody(InputStream is)
			throws IOException {

		StringBuilder body = new StringBuilder();
		byte[] buffer = new byte[2048];
		int length;
		while ((length = is.read(buffer)) != -1) {
			body.append(new String(buffer, 0, length));
		}
		is.close();

		return body;
	}

}
