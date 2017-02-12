import java.util.Arrays;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

// TODO: add double constructor and stop casting on instantiation
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
				// value from all of those values
				for (int i = -1 * w; i <= 1 * w; i++)
				{
					for (int j = -1 * w; j <= 1 * w; j++)
					{
						if (this.pixelExists(x + i, y + j))
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
				sumR = clamp(Math.abs(sumR / power));
				sumG = clamp(Math.abs(sumG / power));
				sumB = clamp(Math.abs(sumB / power));

				// keeps track of what the color for each pixel in the new image
				// should be so we don't modify the same pixels we're reading
				// from
				newColors[y][x] = new Color(sumR, sumG, sumB, 1.0f);
			}
		}

		// sets the color of the new images pixels to the new values
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
		// int w = 2;

		float[][] blurKernel = new float[2 * w + 1][2 * w + 1];

		for (float[] row : blurKernel)
		{
			Arrays.fill(row, 1.0f);
		}

		this.applyKernel(blurKernel, w);
	}


	public void sharpen()
	{
		int w = 1; /// 3x3 kernel
		float[][] sharpenKernel = new float[2 * w + 1][2 * w + 1];

		sharpenKernel[w][w] = 5;
		sharpenKernel[w][0] = -1;
		sharpenKernel[0][w] = -1;
		sharpenKernel[w][2] = -1;
		sharpenKernel[2][w] = -1;

		this.applyKernel(sharpenKernel, w);
	}


	public void invert()
	{
		for (int y = 0; y < this.getHeight(); y++)
		{
			for (int x = 0; x < this.getWidth(); x++)
			{
				this.getPixelWriter().setColor(x, y, this.getPixelReader().getColor(x, y).invert());
			}
		}
	}


	private boolean pixelExists(int x, int y)
	{
		return (x < this.getWidth() && x >= 0) && (y < this.getHeight() && y >= 0);
	}


	private float clamp(float f)
	{
		return f > 1 ? 1.0f : f;
	}

}
