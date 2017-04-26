import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

// TODO: possible methods: enlarge, shrink, rotate, flip
public class MyImage extends WritableImage
{
	private final PixelReader PIXEL_READER = this.getPixelReader();
	private final PixelWriter PIXEL_WRITER = this.getPixelWriter();


	public MyImage(int width, int height)
	{
		super(width, height);
	}


	public MyImage(double width, double height)
	{
		super((int) width, (int) height);
	}


	public MyImage(Image image)
	{
		this(image.getWidth(), image.getHeight());

		for (int y = 0; y < this.getHeight(); y++)
		{
			for (int x = 0; x < this.getWidth(); x++)
			{
				PIXEL_WRITER.setColor(x, y, image.getPixelReader().getColor(x, y));
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
				if (PIXEL_READER.getColor(x, y).getOpacity() == 0)
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
							Color currPixel = PIXEL_READER.getColor(x + i, y + j);
							float kernelValue = kernel[j + w][i + w];

							// sum of rgb values of the averaged pixels
							sumR += currPixel.getRed() * kernelValue;
							sumG += currPixel.getGreen() * kernelValue;
							sumB += currPixel.getBlue() * kernelValue;

							power += kernelValue;
						}

					}
				}

				if (power != 0.0f)
				{
					sumR /= power;
					sumG /= power;
					sumB /= power;
				}

				// keeps track of what the color for each pixel in the new image
				// should be so we don't modify the same pixels we're reading
				// from
				newColors[y][x] = new Color(clamp(sumR), clamp(sumG), clamp(sumB), 1.0f);
			}
		}

		// sets the color of the new images pixels to the new values
		for (int y = 0; y < this.getHeight(); y++)
		{
			for (int x = 0; x < this.getWidth(); x++)
			{
				PIXEL_WRITER.setColor(x, y, newColors[y][x]);
			}
		}
	}


	public void blur(int w)
	{
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
				PIXEL_WRITER.setColor(x, y, PIXEL_READER.getColor(x, y).invert());
			}
		}
	}


	public void edge_detection()
	{
		int w = 1;

		float[][] edgeKernel = new float[2 * w + 1][2 * w + 1];

		for (float[] row : edgeKernel)
		{
			Arrays.fill(row, -1.0f);
		}

		edgeKernel[w][w] = 8.0f;

		this.applyKernel(edgeKernel, w);
	}


	public void reduce_colors(int numPasses, int colorCount)
	{
		// int colorCount = 3;
		List<Color> finalColors = new ArrayList<Color>();

		// start with 3 random colors
		for (int i = 0; i < colorCount; i++)
		{
			finalColors.add(new Color(Math.random(), Math.random(), Math.random(), 1));
		}

		// make a number of passes over the image to see how close these three
		// colors are to colors in the image. For each pixel in the image, place
		// it in the list of colors at each index in groupVotes. For example,
		// groupVotes[1] will contain a color entry for each pixel in the image
		// whose color was closest to the first of our 3 colors.
		// TODO: should this be broken into more methods?
		for (int pass = 0; pass < numPasses; pass++)
		{
			List<List<Color>> groupVotes = new ArrayList<List<Color>>();

			for (int i = 0; i < colorCount; i++)
			{
				groupVotes.add(new ArrayList<Color>());
			}

			for (int y = 0; y < this.getHeight(); y++)
			{
				for (int x = 0; x < this.getWidth(); x++)
				{
					double closestDistance = Double.MAX_VALUE;
					int closestColorIndex = -1;
					Color thisColor = PIXEL_READER.getColor(x, y);

					// get the color of the current pixel in the image and see
					// how close it is to the colors in our list
					for (int guess = 0; guess < colorCount; guess++)
					{
						Color possibleColor = finalColors.get(guess);
						double currentDistance = colorDistance(thisColor, possibleColor);

						if (currentDistance >= closestDistance)
						{
							continue;
						}

						closestColorIndex = guess;
						closestDistance = currentDistance;
					}

					// this color was close to one of our 3 choices, so add it
					// to that choice's index
					groupVotes.get(closestColorIndex).add(thisColor);
				}
			}

			// calculate new median value
			for (int i = 0; i < colorCount; i++)
			{
				double sumR = 0;
				double sumG = 0;
				double sumB = 0;

				// if there were no votes for this color then pick a new random
				// one
				if (groupVotes.get(i).size() == 0)
				{
					sumR = Math.random();
					sumG = Math.random();
					sumB = Math.random();
				}
				// at each of the 3 choices indices, see which color of those
				// that were close to that color occur the most
				else
				{
					for (Color color : groupVotes.get(i))
					{
						sumR += color.getRed();
						sumG += color.getGreen();
						sumB += color.getBlue();
					}

					sumR /= groupVotes.get(i).size();
					sumG /= groupVotes.get(i).size();
					sumB /= groupVotes.get(i).size();
				}

				// set the color we will use in the reduction to the one with
				// the most votes
				finalColors.set(i, new Color(sumR, sumG, sumB, 1));
			}
		}

		// loop the image again and set each pixel to whichever of our 3
		// modified choices it is closest to
		for (int y = 0; y < this.getHeight(); y++)
		{
			for (int x = 0; x < this.getWidth(); x++)
			{
				double closestDistance = Double.MAX_VALUE;
				Color closestColor = null;
				Color thisColor = PIXEL_READER.getColor(x, y);

				for (Color possibleColor : finalColors)
				{
					double currentDistance = colorDistance(thisColor, possibleColor);

					if (currentDistance >= closestDistance)
					{
						continue;
					}

					closestColor = possibleColor;
					closestDistance = currentDistance;
				}

				PIXEL_WRITER.setColor(x, y, closestColor);
			}
		}
	}


	private boolean pixelExists(int x, int y)
	{
		return (x < this.getWidth() && x >= 0) && (y < this.getHeight() && y >= 0);
	}


	private float clamp(float f)
	{
		return Math.abs(f) > 1 ? 1 : Math.abs(f);
	}


	private double colorDistance(Color one, Color two)
	{
		return Math.abs(one.getRed() - two.getRed()) + Math.abs(one.getGreen() - two.getGreen())
				+ Math.abs(one.getBlue() - two.getBlue());
	}

}
