package pdl.backend;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.RectangleShape;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public class Convolution {

	/**
	 * Question 1.1
	 */
	
	public static void meanFilterSimple(final Img<UnsignedByteType> input, final Img<UnsignedByteType> output){
			final RandomAccess<UnsignedByteType> rOutput = output.randomAccess();

			final long[] dimensions = new long[3];
			input.dimensions(dimensions);
			
			final int iw = (int) dimensions[0];
			final int ih = (int) dimensions[1];
			
			// On ne traite pas les bords
			for (int x = 1; x < iw - 1; ++x) {
				for (int y = 1; y < ih - 1; ++y) {
					// Regarde la valeur des pixels l'entourant
					int sum = 0; 	
					final RandomAccess<UnsignedByteType> r2 = input.randomAccess();
					// Filtre moyenneur de 3*3
					for (int x2 = x - 1; x2 <= x + 1; x2++)
						for (int y2 = y - 1; y2 <= y + 1; y2++) {	
							r2.setPosition(x2, 0);
							r2.setPosition(y2, 1);
							int color = r2.get().get();
							sum += color;
						}
						int newcolor = sum / 25;
						rOutput.setPosition(x,0);
						rOutput.setPosition(y,1);
						rOutput.get().set(newcolor);
				}
			}
		}

	/**
	 * Question 1.2
	 */
	// Extend version --> etend l'image à l'infini avec des pixels à 0
	public static void meanFilterWithBordersEXT(final Img<UnsignedByteType> input, final Img<UnsignedByteType> output, int size) {
		final ExtendedRandomAccessibleInterval<UnsignedByteType, Img<UnsignedByteType>> extendedView = Views.extendZero(input);
		final RandomAccess<UnsignedByteType> out = output.randomAccess();

		final long[] dimensions = new long[3];
		input.dimensions(dimensions);
		
		final int iw = (int) dimensions[0];
		final int ih = (int) dimensions[1];
			
		// Parcours image
		for (int x = 0; x < iw; ++x) {
			for (int y = 0; y < ih; ++y) {
				RandomAccessibleInterval< UnsignedByteType > part = Views.interval( extendedView, new long[] { x - (size/2), y - (size/2)}, new long[]{ x + (size/2), y + (size/2)} );
				int sum = 0;
				final Cursor< UnsignedByteType > c = Views.iterable( part ).cursor();
				// Parcours noyau de convolution
				while (c.hasNext()){
					c.fwd();
					sum += c.get().get();
				}
				int newcolor = sum / (size*size);
				out.setPosition(x, 0);
				out.setPosition(y,1);
				out.get().set(newcolor);
			}
		}

	}
	// Expand version --> etend l'image avec les parametres données avec des pixels de la bordure repeté
	public static void meanFilterWithBordersEXP(final Img<UnsignedByteType> input, final Img<UnsignedByteType> output, int size) {
		final IntervalView<UnsignedByteType> expandeddView = Views.expandMirrorDouble(input, size/2, size/2);
        final RandomAccess<UnsignedByteType> out = output.randomAccess();
        
        final int iw = (int) input.max(0);
        final int ih = (int) input.max(1);
        
		// Parcours image
        for (int x = 0; x < iw; ++x) {
            for (int y = 0; y < ih; ++y) {
                RandomAccessibleInterval< UnsignedByteType > part = Views.interval( expandeddView, new long[] { x - size/2, y - size/2 }, new long[]{ x + size/2, y + size/2} );
                int sum = 0;
                final Cursor< UnsignedByteType > c = Views.iterable( part ).cursor();
				// Parcours noyau de convolution
                while (c.hasNext()){
                    c.fwd();
                    sum += c.get().get();
                }
                int newcolor = sum / (size*size);
                out.setPosition(x, 0);
                out.setPosition(y,1);
                out.get().set(newcolor);
            }
        }
	}
	/**
	 * Question 1.3
	 */
	public static void meanFilterWithNeighborhood(final Img<UnsignedByteType> input, final Img<UnsignedByteType> output, int size) {

        final RandomAccess<UnsignedByteType> out = output.randomAccess();
		final IntervalView<UnsignedByteType> expandedView = Views.expandMirrorDouble(input, -size, -size );

		final RectangleShape shape = new RectangleShape( size/2, true );

		//crée un voisinage pour chacun des points sources
		for ( final Neighborhood< UnsignedByteType > localNeighborhood : shape.neighborhoods( expandedView ) ) {
			int CoordX = localNeighborhood.localizingCursor().getIntPosition(0);
			int CoordY = localNeighborhood.localizingCursor().getIntPosition(1);	
			
			int sum = 0;
			// Parcours noyau de convolution
			for ( final UnsignedByteType value : localNeighborhood ){
				sum += value.get();
			}
			int newcolor = sum / (size*size);
			out.setPosition(CoordX,0);
			out.setPosition(CoordY,1);
			out.get().set(newcolor);
		}
	}

	/**
	 * Question 2.1
	 */
	public static void convolution(final Img<UnsignedByteType> input, final Img<UnsignedByteType> output, int[][] kernel)
	{
		int size = kernel.length;
		final IntervalView<UnsignedByteType> expandeddView = Views.expandMirrorDouble(input, size, size);
        final RandomAccess<UnsignedByteType> out = output.randomAccess();

		final long[] dimensions = new long[2];
		input.dimensions(dimensions);
		long iw = dimensions[0];
		long ih = dimensions[1];

		// Calcul coef
		int coef = 0;
		for(int i = 0; i < kernel.length; i ++)
			for(int j = 0; j < kernel[0].length; j ++ )
				coef += kernel[i][j];
		// Parcours image
		for (int x = 0; x < iw; ++x) {
            for (int y = 0; y < ih; ++y) {
                RandomAccessibleInterval< UnsignedByteType > part = Views.interval( expandeddView, new long[] { x - size/2, y - size/2 }, new long[]{ x + size/2 , y + size/2 } );
                int sum = 0;
				final Cursor< UnsignedByteType > c = Views.iterable( part ).cursor();
				int kx = 0, ky = 0;
				// Parcours noyau convolution
                while (c.hasNext()){
					c.fwd();
					if(ky == size ){
						kx ++;
						ky = 0;
					}
					sum += c.get().get()*kernel[kx][ky]; // couleur * coefficient du filtre
					ky ++;
				}

                int newcolor = sum / coef;
                out.setPosition(x, 0);
                out.setPosition(y,1);
                out.get().set(newcolor);
            }
        }
	}

	public static void convolution2(final Img<UnsignedByteType> input, final Img<UnsignedByteType> output, int[][] kernel) {

		int size = kernel[0].length;
		final IntervalView<UnsignedByteType> expandedView = Views.expandMirrorDouble(input, (size/2), (size/2) );
		final RandomAccess<UnsignedByteType> in = expandedView.randomAccess();
		final RandomAccess<UnsignedByteType> out = output.randomAccess();

		final long[] dimensions = new long[2];
		input.dimensions(dimensions);
		long iw = dimensions[0];
		long ih = dimensions[1];

		// Calcule coef
		int coef = 0;
		for(int i = 0; i < kernel.length; i ++)
			for(int j = 0; j < kernel[0].length; j ++ )
				coef += kernel[i][j];
		// Parcours image
		for (int x = 0 ; x < iw ; x++){
			for (int y = 0 ; y < ih ; y++){
				int sum = 0;

				int Kx = 0;
				int Ky = 0;
				// Parcours noyau convolution
				for (int x2 = x - size/2; x2 <= x + size/2; x2++){
					for (int y2 = y - size/2; y2 <= y + size/2; y2++) {
							in.setPosition(x2,0);
							in.setPosition(y2,1);
							sum += in.get().get()*kernel[Kx][Ky];
							Ky ++;
					}
					Kx ++;
					Ky = 0;
				}
				sum /= coef;
				out.setPosition(x,0);
				out.setPosition(y,1);
				out.get().set(sum);
			}
		}
	}
	/**
	 * Question 2.3
	 */
	public static void gaussFilterImgLib(final Img<UnsignedByteType> input, final Img<UnsignedByteType> output) {
		final ExtendedRandomAccessibleInterval<UnsignedByteType, Img<UnsignedByteType>> extendedView = Views.extendZero(input);
		final int n = input.numDimensions();
		final double[] s = new double[ n ];
		for ( int d = 0; d < n; ++d )
			s[ d ] = 4.0/3.0;

		Gauss3.gauss( s, extendedView, output );
		// // VERSION THREADS
		// final int numthreads = Runtime.getRuntime().availableProcessors();
		// final ExecutorService service = Executors.newFixedThreadPool( numthreads );
		// Gauss3.gauss( s, extendedView, output, service );
		// service.shutdown();
	}
	public static void convolutionSobel(final Img<UnsignedByteType> input, final Img<UnsignedByteType> output)
	{
		// final Dimensions dim = input;
		// final ArrayImgFactory<UnsignedByteType> factory = new ArrayImgFactory<>(new UnsignedByteType());
		// final Img<UnsignedByteType> output = factory.create(dim);

		double[][]kernelX = {{1,0,-1}, {2,0,-2}, {1,0,-1}};
		double[][]kernelY = {{1,2,1}, {0,0,0}, {-1,-2,-1}};

		int size = 3; // length kernelY
		final IntervalView<UnsignedByteType> expandeddView = Views.expandMirrorDouble(input, size, size);
        final RandomAccess<UnsignedByteType> out = output.randomAccess();

		final long[] dimensions = new long[2];
		input.dimensions(dimensions);
		long iw = dimensions[0];
		long ih = dimensions[1];

		// Parcours image
		for (int x = 0; x < iw; ++x) {
            for (int y = 0; y < ih; ++y) {
                RandomAccessibleInterval< UnsignedByteType > part = Views.interval( expandeddView, new long[] { x - size/2, y - size/2 }, new long[]{ x + size/2 , y + size/2 } );
                double newval = 0;
				double gx = 0; double gy = 0;
				final Cursor< UnsignedByteType > c = Views.iterable( part ).cursor();
				int kx = 0, ky = 0;
				// Parcours noyau convolution
                while (c.hasNext()){
					c.fwd();
					if(ky == size ){
						kx ++;
						ky = 0;
					}
					gx =+ c.get().get()*kernelX[kx][ky];
					gy =+ c.get().get()*kernelY[kx][ky];
					// newval += Math.sqrt(Math.pow((c.get().get()*kernelX[ky][kx]),2) + Math.pow((c.get().get()*kernelY[ky][kx]),2)); // couleur * coefficient du filtre
					ky ++;
				}
				newval = Math.sqrt((gx*gx) + (gy*gy));
				// newval = Math.abs(newval);
				if(newval < 0) newval = 0;
            	if(newval > 255) newval = 255;
                int newcolor = (int) newval;
                out.setPosition(x, 0);
                out.setPosition(y,1);
                out.get().set(newcolor);
            }
        }
	}
}

