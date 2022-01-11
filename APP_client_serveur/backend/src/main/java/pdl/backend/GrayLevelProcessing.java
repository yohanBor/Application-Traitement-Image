package pdl.backend;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.UnsignedByteType;

public class GrayLevelProcessing{

	public static int getPxMin(Img<UnsignedByteType> img){
		int min = 255;
		final Cursor< UnsignedByteType > cursor = img.cursor();
		while( cursor.hasNext() )
		{
			cursor.fwd();
			int color = cursor.get().get();
			if(color < min)
				min = color;
		}
		return min;
	}
	public static int getPxMax(Img<UnsignedByteType> img){
		int max = 0;
		final Cursor< UnsignedByteType > cursor = img.cursor();
		while( cursor.hasNext() )
		{
			cursor.fwd();
			int color = cursor.get().get();
			if(color > max)
				max = color;
		}
		return max;
	}
	// ----------- CODE D'EXEMPLE -----------
	public static void threshold(Img<UnsignedByteType> img, int t) {
		final RandomAccess<UnsignedByteType> r = img.randomAccess();

		final int iw = (int) img.max(0);
		final int ih = (int) img.max(1);

		for (int x = 0; x <= iw; ++x) {
			for (int y = 0; y <= ih; ++y) {
				r.setPosition(x, 0);
				r.setPosition(y, 1);
				final UnsignedByteType val = r.get();
				if (val.get() < t)
				    val.set(0);
				else
				    val.set(255);
			}
		}
	}
	// ----------- 2) LUMINOSITE-----------
	// On regle la luminosité d'une image en augmentant / diminuant la valeur de chaque pixel par un delta choisi
	// 2.1
	public static void graybrightness(Img<UnsignedByteType> img, int delta)
	{
		final RandomAccess<UnsignedByteType> r = img.randomAccess(); // Utilisé pour acceder aux pixels

		final int iw = (int) img.max(0);
		final int ih = (int) img.max(1);

		for (int x = 0; x <= iw; ++x) {
			for (int y = 0; y <= ih; ++y) {
				r.setPosition(x, 0);
				r.setPosition(y, 1);
				final UnsignedByteType val = r.get();
				int newGray = val.get() + delta;
				if(newGray > 255)
					val.set(255);
				else if(newGray < 0)
					val.set(0);
				else
					val.set(newGray);
			}
		}
	}
	// use cursor : https://imagej.net/ImgLib2_-_Accessors.html#Cursor
	// 2.2
	public static void graybrightnessCursor(Img<UnsignedByteType> img, int delta)
	{
		final Cursor< UnsignedByteType > cursor = img.cursor();

		while( cursor.hasNext() )
		{
			cursor.fwd();
			int newGray = cursor.get().get() + delta; // get the pixel then get the color of this pixel
			if(newGray > 255)
				cursor.get().set(255) ;
			else if(newGray < 0)
				cursor.get().set(0) ;
			else 
				cursor.get().set(newGray) ;
		}

	}
	// ----------- 3 Contraste) DYNAMIQUE -----------

	public static void contrast(Img<UnsignedByteType> img ) //3.1
	{
		final Cursor< UnsignedByteType > cursor = img.cursor();
		int pixelMax =  getPxMax(img);
		int pixelMin =  getPxMin(img);
		while( cursor.hasNext() )
		{
			cursor.fwd();
			int color = cursor.get().get();
			int newcolor = ((255 *(color - pixelMin)) /( pixelMax - pixelMin)) ; // formule du cours
			if(newcolor > 255)
				cursor.get().set(255) ;
			else if(newcolor < 0)
				cursor.get().set(0) ;
			else 
				cursor.get().set(newcolor);
		}
	}
	public static void contrast(Img<UnsignedByteType> img, int min, int max ) // 3.2
	{
		final Cursor< UnsignedByteType > cursor = img.cursor();
		int pixelMax =  getPxMax(img);
		int pixelMin =  getPxMin(img);
		while( cursor.hasNext() )
		{
			cursor.fwd();
			int color = cursor.get().get();	
			int newcolor = (((max - min) *(color - pixelMin)) /( pixelMax - pixelMin)) + min; // formule adaptée
			if(newcolor > 255)
				cursor.get().set(255) ;
			else if(newcolor < 0)
				cursor.get().set(0) ;
			else 
				cursor.get().set(newcolor);
		}
		// Verifions que nous sommes bien dans l'intervalle demandée :
		//  System.out.println("New pixel min = " + getPxMin(img));
		//  System.out.println("New pixel max= " + getPxMax(img));
	}
	
	public static void lookuptable(Img<UnsignedByteType> img, int min, int max ) // 3.3
	{
		int pixelMax =  getPxMax(img);
		int pixelMin =  getPxMin(img);

		int[] LUT = new int[256];
		for(int ng = 0; ng < 256; ng ++)
		{
			LUT[ng] = ((max - min) * (ng - pixelMin)) / (pixelMax -pixelMin) + min;
		}
		final Cursor< UnsignedByteType > cursor = img.cursor();
		while( cursor.hasNext() )
		{
			cursor.fwd();
			int color = cursor.get().get();
			cursor.get().set(LUT[color]); // accée look up table
		}
	}
	// ----------- 4 Contraste) Egalisation d'histogramme -----------
	
	public static void Ehistogramme(Img<UnsignedByteType> img ) // 4
	{
		int[] table = new int[256];

		Cursor< UnsignedByteType > cursor = img.cursor();
		// Calcul histogramme
		while( cursor.hasNext() )
		{
			cursor.fwd();
			int color = cursor.get().get();
			table[color] += 1;
		}
		// Calcul histogramme cumulé
		cursor = img.cursor();
		int[] tableC = new int[256];
		tableC[0] = table[0];
		for(int i = 1; i < 256; i ++){
			tableC[i] = tableC[i - 1] + table[i];
		}

		// Transformation niveau de gris
		cursor = img.cursor();
		while( cursor.hasNext() )
		{
			cursor.fwd();
			int color = cursor.get().get();
			long newcolor = (tableC[color] * 255) / (img.max(0) * img.max(1) + 1);
			cursor.get().set((int)newcolor);
		}
	}
}