import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class MyImage extends WritableImage
{

	public MyImage(int width, int height)
	{
		super(width, height);
	}


	public MyImage(Image image)
	{
		super((int) image.getWidth(), (int) image.getHeight());

		for (int y = 0; y < this.getHeight(); y++)
		{
			for (int x = 0; x < this.getWidth(); x++)
			{
				this.getPixelWriter().setColor(x, y, image.getPixelReader().getColor(x, y));
			}
		}
	}


	private void applyKernel(float[][] kernel, int w)
	{
		Color[][] newColors = new Color[(int) this.getHeight()][(int) this.getWidth()];

		for (int y = 0; y < this.getHeight(); y++)
		{
			for (int x = 0; x < this.getWidth(); x++)
			{
				if (this.getPixelReader().getColor(x, y).getOpacity() == 0)
				{
					newColors[y][x] = Color.TRANSPARENT;
					continue;
				}

				float sumR = 0;
				float sumG = 0;
				float sumB = 0;

				float power = 0;

				// loop around 'w' pixels from the current pixel and average a
				// blurred value from all of those values (higher 'w' means more
				// blur)
				for (int i = -1 * w; i <= 1 * w; i++)
				{
					for (int j = -1 * w; j <= 1 * w; j++)
					{
						if (this.doesPixelExist(x + i, y + j))
						{
							// get color of the pixel 'w' pixels away in any
							// direction
							Color currPixel = this.getPixelReader().getColor(x + i, y + j);
							float kernelValue = kernel[j + w][i + w];

							// sum of rgb values of the averaged pixels
							sumR += currPixel.getRed() * kernelValue;
							sumG += currPixel.getGreen() * kernelValue;
							sumB += currPixel.getBlue() * kernelValue;

							power += kernelValue;
						}

					}
				}

				// rgb values of the blurred pixel
				sumR /= power;
				sumG /= power;
				sumB /= power;

				// keeps track of what the color for each pixel in the new image
				// should be so we don't sample off of the same pixels we're
				// blurring
				newColors[y][x] = new Color(sumR, sumG, sumB, 1.0f);
			}
		}

		// sets the color of the new images pixels to the blurred values
		for (int y = 0; y < this.getHeight(); y++)
		{
			for (int x = 0; x < this.getWidth(); x++)
			{
				this.getPixelWriter().setColor(x, y, newColors[y][x]);
			}
		}
	}


	public void blur(int w)
	{

		float[][] blurKernel = new float[2 * w + 1][2 * w + 1];

		for (int y = 0; y < 2 * w + 1; y++)
		{
			for (int x = 0; x < 2 * w + 1; x++)
			{
				blurKernel[y][x] = 1;
			}
		}

		this.applyKernel(blurKernel, w);

	}


	private boolean doesPixelExist(int x, int y)
	{
		return (x < this.getWidth() && x >= 0) && (y < this.getHeight() && y >= 0);
	}

}
