/* This is the only source file that ever needs to be modified.
 Upon compilation, it will spit out two class files. _Both_ of them
 must be present in a folder for the applet to run, but Cubic.class
 is the one to be inserted in HTML code.
 The file can be renamed, but then the main class (currently Cubic)
 has to be renamed as well. The file must have the same name as
 the main class.
 I will mark the places that must be changed (when you change
 the function) with uppercase ATTENTION.
 Here is a checklist of what needs to be done when you replace one
 function with another:

-(Optional.) Give the parameter a new name (e.g. W instead of C) in initForm
-Rewrite the functions iterFunction_R and iterFunction_I
-Change the value of the critical point in the function recalculate_Eps
-Replace the starting values of the corners of the screen in the "init" function.
-Replace the starting value of the parameters in the "init" function.
*/

import java.awt.*;
import java.applet.*;
import java.awt.event.*;

// The declaration of C_Screen is technical and does not need
// to be modified.
class C_Screen extends Canvas
{
 Image MyImage;
 public void paint (Graphics g)
 {
  g.drawImage(MyImage, 0, 0, this);
 }
 
 public void update(Graphics g)
 {
  paint(g);
 }
}

public class Cubic extends Applet
 implements ActionListener, MouseListener, Runnable
{
/* This class and its elements contain the bulk of the code.
 I will describe the 'mathematical' variables in somewhat
 greater detail than the 'technical' variables. */
 
 boolean Z_Redrawing;
 boolean E_Redrawing;
 boolean NewOrbit;
 boolean BlackAndWhite;
 boolean SoftenColors;
/*  The boolean variables don't have to be modified. They
 contain the answers to the questions: "Do I have to redraw
 the Z-plane this time?" "Do I have to redraw the parameter
 plane?" "Is there a point that I need to iterate?" "Is the
 applet going to be in black and white?" and "Are there so
 many iterations that I need to shift my color scale?"*/ 
 double RealC, ImagC;
/* I do not use the class "complex" in the applet. A complex
 variable is represented as a pair of real variables. Here,
 RealC and ImagC represent the parameter of the function.
 Parameters come in all sizes and shapes. Since most of the
 functions of interest have only one, I suggest keeping the
 name C in the code, regardless of what the thing is actually
 called (epsilon, k, etc.)*/
 int MaxIter;
/* This is how many iterations we have to perform in the Z-screen.
 A point is iterated until it escapes to an enormous radius, or
 until this number of iterations is performed. */
 int EpsMaxIter;
/* This is the same as the previous one, but for the parameter plane*/
 byte MaxAnim;
/* This is related to the part of the applet that prepares animations
 in the Z-plane. Animations show how the picture changes as we go
 from one point in the parameter plane to the next. It is possible
 to load chains of points, not just single segments. This specifies
 how long such a chain can be.*/
 int Iterations;
/* This is different from MaxIter. MaxIter shows the maximum number
 of iterations, as used for the drawing of the Z-plane. Iterations
 is read when the user selects a single point and says, "Run this
 many iterations of that point." The variable stores the number
 "this many".
 Iterations can be greater than MaxIter. If it is, one can sometimes
 see a point escape from a pitch-black region, just because the
 applet did not perform enough iterations to realize that the point
 does eventually escape.*/
 double CritRadius;
/* No surprises here. For the purposes of escaping points, this number
 is infinity. Once an orbit is outside this radius, we can stop
 iterating.*/
 double Xmin, Xmax, Ymin, Ymax;
/* These variables indicate which region of the complex plane has to
 be displayed on the Z-screen. (Xmin + i Ymin) is the lower left
 corner, (Xmax+i Ymax), the upper right corner. The region does not
 have to be a square. */
/* X and Y are always supposed to mean Re(z) and Im(z). I hope this
 does not generate too much confusion.*/ 
 double EpsPlaneXmin, EpsPlaneXmax, EpsPlaneYmin, EpsPlaneYmax;
/* These are the same as the previous four variables, except that
 they work for the parameter plane. Again, they mark the lower
 left and the upper right corners.*/
 double RealCoord, ImagCoord;
/* The coordinates of the point that the user has selected to iterate.*/
 double Xincr, Yincr;
/* The increment of x and y. Somewhere along the way the applet
 will compute them as (Xmax - Xmin)/300 and the same for y. 300
 pixels is the default width and height of either screen.*/
 double EpsPlaneXincr, EpsPlaneYincr;
/* These are the increments for the parameter plane, computed
 by the same formula.*/
 double SeedX, SeedY;
/* The coordinates of the point whose orbit is about to be displayed.*/ 
 int ResultArray[][];
/* This enormous array will store the color of each point in the z-plane.
 When the iteration is run, it 90,000 entries are filled one by one.*/
 int EpsPlaneResultArray[][];
/* An equally enormous beast for the parameter plane.*/
 double OrbitXs[], OrbitYs[];
/* This somewhat smaller array stores the successive iterates of the point
 selected by the user. Its (actual) length is the value of the variable
 Iterations.*/
 double AnimXs[], AnimYs[];
/* These two arrays are used when the applet cooks an animation. They store
 the value of the parameter for each successive frame.*/
 double ArchXmin[], ArchXmax[], ArchYmin[], ArchYmax[];    
 double ArchEpsXMin[], ArchEpsXMax[], ArchEpsYMin[], ArchEpsYMax[];
 double ArchRealC[], ArchImagC[];
 int ArchMaxIter[], ArchEpsMaxIter[];
 boolean ArchZRedraw[], ArchEpsRedraw[];
 int RollBackTotal, RollBackIndex;
/* The many "Arch-" arrays are used for rolling back both the z- and
 the parameter plane. They store the previous values of all the
 variables. They are arrays, as opposed to variables, to make it possible
 to roll back multiple times. */
 double BaseXmin, BaseXmax, BaseYmin, BaseYmax;
 double BaseEpsXmin, BaseEpsXmax, BaseEpsYmin, BaseEpsYmax;
 double BaseRealC, BaseImagC;
 int BaseMaxIter, BaseEpsMaxIter;
/* The "Base-" variables are used when the user clicks "Restore default"
 They store the original values of all the parameters.*/ 
 final static int RollBackMax=5;
/* This is how many times the pictures can be rolled back. The applet
 remembers the last five pictures, but forgets everything before that.
 RollBackMax can be changed, but for most demonstrations five is enough.*/
 final static int ScreenSize=300;
/* This, on the other hand, should not be altered. It shows how many pixels
 there are in each screen.*/


/* Next, the technical things are declared. They are the elements of
 the main applet screen. This part does not need to be changed, unless
 it is necessary to add other parameters. The names are for the most
 part self-explanatory.*/
 Button RedrawButton;
 Button IterateButton;
 Button NextIterButton;
 Button AddEpsButton;
 Button RemoveEpsButton;
 Button AnimateButton;
 Button RestoreButton;
 Button RollBackButton;
 
/* Text fields are the small white windows where the user enters numbers.*/
 TextField ReZMinTextField;
 TextField ReZMaxTextField;
 TextField ImZMinTextField;
 TextField ImZMaxTextField;
 TextField ReEpsMinTextField;
 TextField ReEpsMaxTextField;
 TextField ImEpsMinTextField;
 TextField ImEpsMaxTextField;
 TextField RealCTextField;
 TextField ImagCTextField;
 TextField RealIterTextField;
 TextField ImagIterTextField;
 TextField IterationsTextField;
 TextField ZMaxIterTextField;
 TextField EpsMaxIterTextField;
 Label Explanations[];
/* I collected all the labels in one array called 'Explanations'.
 It contains all the text that hangs in the applet window. In particular,
 the label "m =", which should really be "d =", is Explanations[9]).
 */

 List OrbitList;
 List AnimList;
/* These are the two lists in the applet screen. A list is a vertical
 array of numbers on white background. The first list is for successive
 iterates of the selected point, the second one, for the breakpoints
 stored for of animation.*/
 
 Image ZPlaneImage;
 Graphics ZPlaneGraphics;
 C_Screen ZPlaneCanvas;
 Image EpsPlaneImage;
 Graphics EpsPlaneGraphics;
 C_Screen EpsPlaneCanvas;
/* A bunch of graphic technicalities that I copied from a book on Java.*/
 int MouseDownX, MouseDownY, MouseUpX, MouseUpY;
/* These variables are used for zooming with the mouse. They record the
 coordinates where the user pressed the left mouse button,
 and where it was released.*/
 byte MouseDownScreen;
 byte RectDrawScreen;
 Font MainFont;
/* MouseDownScreen indicates in which screen the mouse was last pressed,
 as follows: 1 means ZPlaneCanvas, 2 means EpsPlaneCanvas, 0 means neither
 RectDrawScreen indicates the screen on which the white zooming rectangle
 should be drawn, in the same fashion.*/
 
 byte AnimBreakPoints;
 boolean NewMovie;
 int FramesPerSegment;
 Thread AnimationThread;
 Image[] AnimImageSequence;
 Graphics[] AnimGraphicsSequence;
/* Nuts and bolts for running animations.*/

 Label StatusLabel;
/* This is not included in the Explanations, because you normally can't
 see it. It is only displayed when the applet is preparing an animation,
 and it indicates how many frames have already been computed.*/

 public void init()
 { 
/*  The 'init' function assigns initial values to each of the variables.
  It is responsible for what will actually be displayed when the applet
  runs first. When you change your applet to work with a new function,
  this is usually the last thing you modify. You first get the program
  to run with the new function. Then, by trial and error, you find an
  interesting value of the parameter and adjust the corners of both
  screens to show the entire fractal.*/
  int i, j;
  RealC=0;
  ImagC=0;
  /*ATTENTION: These two variables have to be changed. They keep the
  value of the parameter that is used when the applet first loads.
  
  A decent starting value for C(z-(z^3)/3) is C = 0 + 1.1 i. In this
  tentacle of the Mandelbrot set the function has a period 4
  attracting orbit.
  
  For Cz(1-z)(1-2z)^2, a beautiful fractal appears when C=-1.3+ 0i*/

  ArchRealC=new double[RollBackMax];
  ArchImagC=new double[RollBackMax];
  ArchRealC[0]=RealC;
  ArchImagC[0]=ImagC;
  MaxIter=100; //ATTENTION: these two variables may need to be changed,
  EpsMaxIter=100; //but 50 tends to work fine for polynomial maps.
  ArchMaxIter=new int[RollBackMax];
  ArchMaxIter[0]=MaxIter;
  ArchEpsMaxIter=new int[RollBackMax];
  ArchEpsMaxIter[0]=MaxIter;
  CritRadius=100;
/*ATTENTION: The "infinity radius" of one hundred is
usually good enough, but for huge values of parameters it will
need to be replaced.*/
  MaxAnim=20;

  /*ATTENTION: the next eight variables have to be changed, otherwise
  the applet won't show the entire fractal upon starting, or will show
  it too small.*/
  
  /* A suitable set of parameters for C(z-(z^3)/3) is
  Xmin=-2.5; Xmax=2.5; Ymin=-2.5; Ymax=2.5; EpsPlaneXmin=-2.5;
  EpsPlaneXmax=2.5; EpsPlaneYmin=-2.5; EpsPlaneYmax=2.5; */
  
  /* For Cz(1-z)(1-2z)^2, among other choices, one can set
  Xmin=-0.3; Xmax=1.3; Ymin=-0.8; Ymax=0.8;
  EpsPlaneXmin=-4; EpsPlaneXmax=8; EpsPlaneYmin=-6; EpsPlaneYmax=6;*/
  
  Xmin=-2.0;
  Ymin=-2.0;
  Xmax=2.0;
  Ymax=2.0;
  EpsPlaneXmin=-1.2;
  EpsPlaneYmin=-1.2;
  EpsPlaneXmax=1.2;
  EpsPlaneYmax=1.2;
  
/*  From here on, the "init" function does not have to be modified.*/
  ArchXmin=new double[RollBackMax];
  ArchXmin[0]=Xmin;
  ArchXmax=new double[RollBackMax];
  ArchXmax[0]=Xmax;
  ArchEpsXMin=new double[RollBackMax];
  ArchEpsXMin[0]=EpsPlaneXmin;
  ArchEpsXMax=new double[RollBackMax];
  ArchEpsXMax[0]=EpsPlaneXmax;
  ArchYmin=new double[RollBackMax];
  ArchYmin[0]=Ymin;
  ArchYmax=new double[RollBackMax];
  ArchYmax[0]=Ymax;
  ArchEpsYMin=new double[RollBackMax];
  ArchEpsYMin[0]=EpsPlaneYmin;
  ArchEpsYMax=new double[RollBackMax];
  ArchEpsYMax[0]=EpsPlaneYmax;
  ArchZRedraw=new boolean[RollBackMax];
  ArchEpsRedraw=new boolean[RollBackMax];
  BaseXmin=Xmin;
  BaseXmax=Xmax;
  BaseYmin=Ymin;
  BaseYmax=Ymax;
  BaseEpsXmin=EpsPlaneXmin;
  BaseEpsXmax=EpsPlaneXmax;
  BaseEpsYmin=EpsPlaneYmin;
  BaseEpsYmax=EpsPlaneYmax;
  BaseRealC=RealC;
  BaseImagC=ImagC;
  BaseMaxIter=MaxIter;
  BaseEpsMaxIter=EpsMaxIter;
  ArchZRedraw[0]=false;
  ArchEpsRedraw[0]=false;
  NewOrbit=false;
  SeedX=0;
  SeedY=0;
  RollBackTotal=1;
  RollBackIndex=0;
  Xincr=(Xmax-Xmin)/ScreenSize;
  Yincr=(Ymax-Ymin)/ScreenSize;
  Iterations=0;
  AnimXs=new double[MaxAnim];
  AnimYs=new double[MaxAnim];
  ResultArray=new int[ScreenSize][ScreenSize];
  EpsPlaneResultArray=new int[ScreenSize][ScreenSize];
  for(i=0; i<ScreenSize; i++)
   for(j=0; j<ScreenSize; j++)
   {
    ResultArray[i][j]=0;
    EpsPlaneResultArray[i][j]=0;
   }

  initForm();
 }
 
// The next few elements don't need to be modified.
 private final String labelParam = "label";
 private final String backgroundParam = "background";
 private final String foregroundParam = "foreground";

 private Color stringToColor(String paramValue)
 {
  int red;
  int green;
  int blue;

  red = (Integer.decode("0x" + paramValue.substring(0,2))).intValue();
  green = (Integer.decode("0x" + paramValue.substring(2,4))).intValue();
  blue = (Integer.decode("0x" + paramValue.substring(4,6))).intValue();

  return new Color(red,green,blue);
 }

 public String[][] getParameterInfo()
 {
  String[][] info =
  {
   { labelParam, "String", "Label string to be displayed" },
   { backgroundParam, "String", "Background color, format \"rrggbb\"" },
   { foregroundParam, "String", "Foreground color, format \"rrggbb\"" },
  };
  return info;
 }

 void initForm()
 {
/*  This function basically says: draw everything where it is
  supposed to be, then run the computations once and draw the
  z-screen and the parameter screen.
  You don't need to modify this function unless you want to give
  the parameter a different name.*/
  int i, j;
  this.setLayout(null);
  this.setBackground(Color.lightGray);
  this.setForeground(Color.black);
  MainFont=new Font("Times New Roman", Font.PLAIN, 12);
  this.setFont(MainFont);
  ZPlaneImage=createImage(ScreenSize, ScreenSize);
  ZPlaneGraphics=ZPlaneImage.getGraphics();
  EpsPlaneImage=createImage(ScreenSize, ScreenSize);
  EpsPlaneGraphics=EpsPlaneImage.getGraphics();
  RectDrawScreen=0;
  MouseDownScreen=0;
  AnimBreakPoints=0;
  FramesPerSegment=8;
  NewMovie=true;

  StatusLabel=new Label("");
  this.add(StatusLabel);
  StatusLabel.setBounds(295, 350, 165, 15);
  StatusLabel.setBackground(getBackground());
  StatusLabel.setAlignment(2);
  StatusLabel.setVisible(false);
  
  RedrawButton=new Button("Compute");
  RedrawButton.addActionListener(this);
  this.add(RedrawButton);
  RedrawButton.setBounds(260, 373, 60, 20);
  
  IterateButton=new Button("View orbit");
  IterateButton.addActionListener(this);
  this.add(IterateButton);
  IterateButton.setBounds(320, 150, 80, 20);
  
  NextIterButton=new Button("Iterate");
  NextIterButton.addActionListener(this);
  this.add(NextIterButton);
  NextIterButton.setBounds(410, 150, 70, 20);

  AddEpsButton=new Button("Add current E");
  AddEpsButton.addActionListener(this);
  this.add(AddEpsButton);
  AddEpsButton.setBounds(326, 305, 80, 20);
  RemoveEpsButton=new Button("Remove");
  RemoveEpsButton.addActionListener(this);
  this.add(RemoveEpsButton);
  RemoveEpsButton.setBounds(412, 305, 60, 20);

  AnimateButton=new Button("Animate");
  AnimateButton.addActionListener(this);
  this.add(AnimateButton);
  AnimateButton.setBounds(325, 373, 60, 20);

  RestoreButton=new Button("Restore default");
  RestoreButton.addActionListener(this);
  this.add(RestoreButton);
  RestoreButton.setBounds(390, 373, 100, 20);
  
  RollBackButton=new Button("Roll back");
  RollBackButton.addActionListener(this);
  this.add(RollBackButton);
  RollBackButton.setBounds(495, 373, 70, 20);

  ReZMinTextField=new TextField(Double.toString(Xmin));
  ReZMaxTextField=new TextField(Double.toString(Xmax));
  ImZMinTextField=new TextField(Double.toString(Ymin));
  ImZMaxTextField=new TextField(Double.toString(Ymax));
  this.add(ReZMinTextField);
  this.add(ReZMaxTextField);
  this.add(ImZMinTextField);
  this.add(ImZMaxTextField);
  ReZMinTextField.setBounds(111, 340, 40, 20);
  ReZMaxTextField.setBounds(111, 365, 40, 20);
  ImZMinTextField.setBounds(160, 340, 40, 20);
  ImZMaxTextField.setBounds(160, 365, 40, 20);
  
  ReEpsMinTextField=new TextField(Double.toString(EpsPlaneXmin));
  ReEpsMaxTextField=new TextField(Double.toString(EpsPlaneXmax));
  ImEpsMinTextField=new TextField(Double.toString(EpsPlaneYmin));
  ImEpsMaxTextField=new TextField(Double.toString(EpsPlaneYmax));
  this.add(ReEpsMinTextField);
  this.add(ReEpsMaxTextField);
  this.add(ImEpsMinTextField);
  this.add(ImEpsMaxTextField);
  ReEpsMinTextField.setBounds(689, 340, 40, 20);
  ReEpsMaxTextField.setBounds(689, 365, 40, 20);
  ImEpsMinTextField.setBounds(738, 340, 40, 20);
  ImEpsMaxTextField.setBounds(738, 365, 40, 20);

  RealCTextField=new TextField(Double.toString(RealC));
  ImagCTextField=new TextField(Double.toString(ImagC));
  this.add(RealCTextField);
  this.add(ImagCTextField);
  RealCTextField.setBounds(362, 68, 40, 20);
  ImagCTextField.setBounds(411, 68, 40, 20);
  
  RealIterTextField=new TextField();
  ImagIterTextField=new TextField();
  IterationsTextField=new TextField("10");
  this.add(RealIterTextField);
  this.add(ImagIterTextField);
  this.add(IterationsTextField);
  RealIterTextField.setBounds(355, 100, 40, 20);
  ImagIterTextField.setBounds(405, 100, 40, 20);
  IterationsTextField.setBounds(373, 125, 30, 20);
  
  ZMaxIterTextField=new TextField(Integer.toString(MaxIter));
  EpsMaxIterTextField=new TextField(Integer.toString(EpsMaxIter));
  this.add(ZMaxIterTextField);
  this.add(EpsMaxIterTextField);
  ZMaxIterTextField.setBounds(245, 330, 30, 20);
  EpsMaxIterTextField.setBounds(518, 330, 30, 20);
  
  OrbitList=new List();
  this.add(OrbitList);
  OrbitList.setBounds(314, 175, 170, 50);
  OrbitList.setVisible(false);
  OrbitList.setMultipleMode(false);

  AnimList=new List();
  this.add(AnimList);
  AnimList.setBounds(314, 250, 170, 50);
  AnimList.setVisible(true);
  AnimList.setMultipleMode(false);

  Explanations=new Label[32];
  Explanations[0]=new Label("Region to be shown on the z-screen:");
  Explanations[1]=new Label("Lower left corner:");
  Explanations[2]=new Label("Upper right corner:");
  Explanations[3]=new Label("+");
  Explanations[4]=new Label("i");
  Explanations[5]=new Label("+");
  Explanations[6]=new Label("i");
  Explanations[7]=new Label("Function parameters:");
//  Explanations[8]=new Label("n =");
//  Explanations[9]=new Label("m =");
  Explanations[10]=new Label("C =");
  //ATTENTION: If you want to call the parameter something other
  //than C, change the text in the quotation marks in the last line,
  //e.g. Explanations[10]=new Label("W ="); or some such.
  Explanations[11]=new Label("+");
  Explanations[12]=new Label("i");
  Explanations[13]=new Label("+");
  Explanations[14]=new Label("i");
  Explanations[15]=new Label("+");
  Explanations[16]=new Label("i");
  Explanations[17]=new Label("Seed:");
  Explanations[18]=new Label("+");
  Explanations[19]=new Label("i");
  Explanations[20]=new Label("Make");
  Explanations[21]=new Label("iterations.");
  Explanations[22]=new Label("Region to be shown on the E-screen:");
  Explanations[23]=new Label("Lower left corner:");
  Explanations[24]=new Label("Upper right corner:");
  Explanations[25]=new Label("+");
  Explanations[26]=new Label("i");
  Explanations[27]=new Label("+");
  Explanations[28]=new Label("i");
  Explanations[29]=new Label("Sequence of E-s for animation:");
  Explanations[30]=new Label("Max. iterations:");
  Explanations[31]=new Label("Max. iterations:");
  for(i=0; i<=31; i++) 
  {
   if ((i!=8)&&(i!=9))
   {
    this.add(Explanations[i]);
   }
  }
  Explanations[0].setBounds(7, 320, 205, 15);
  Explanations[1].setBounds(14, 343, 97, 15);
  Explanations[2].setBounds(6, 368, 105, 15);
  Explanations[3].setBounds(150, 343, 10, 15);
  Explanations[4].setBounds(202, 343, 5, 15);
  Explanations[5].setBounds(150, 368, 10, 15);
  Explanations[6].setBounds(202, 368, 5, 15);
  Explanations[7].setBounds(340, 8, 120, 15);
//  Explanations[8].setBounds(365, 28, 22, 15);
//  Explanations[9].setBounds(362, 48, 22, 15);
  Explanations[10].setBounds(340, 71, 22, 15);
  //ATTENTION: If you change the name of the parameter to something
  //longer than one letter of the English alphabet, you need to set
  //different bounds here.
  //The first number has to be reduced by approximately seven times the
  //number of letters in the name of the parameter beyond the first. The
  //third number has to be increased by the same amount. 
  //E.g. if you want the applet to say "Epsilon =" (and the values), then
  //make the previous line Explanations[10].setBounds(300, 71, 62, 15);
  //Other than for aesthetic purposes, you won't need to change
  //this part.
  
  Explanations[15].setBounds(401, 71, 10, 15);
  Explanations[16].setBounds(452, 71, 5, 15);
  Explanations[17].setBounds(317, 103, 40, 15);
  Explanations[18].setBounds(394, 103, 10, 15);
  Explanations[19].setBounds(445, 103, 5, 15);
  Explanations[20].setBounds(340, 128, 32, 15);
  Explanations[21].setBounds(406, 128, 57, 15);
  Explanations[22].setBounds(586, 320, 205, 15);
  Explanations[23].setBounds(592, 343, 97, 15);
  Explanations[24].setBounds(584, 368, 105, 15);
  Explanations[25].setBounds(728, 343, 10, 15);
  Explanations[26].setBounds(780, 343, 5, 15);
  Explanations[27].setBounds(728, 368, 10, 15);
  Explanations[28].setBounds(780, 368, 5, 15);
  Explanations[29].setBounds(314, 231, 171, 15);
  Explanations[30].setBounds(220, 315, 82, 15);
  Explanations[31].setBounds(493, 315, 82, 15);
  for(i=0; i<=31; i++) 
  {
   if ((i!=8)&&(i!=9))
   {
    Explanations[i].setBackground(getBackground());
   }
  }

  recalculate_Z();
  redrawZPlaneFractal(ZPlaneGraphics);
  ZPlaneCanvas=new C_Screen();
  ZPlaneCanvas.MyImage=ZPlaneImage;
  this.add(ZPlaneCanvas);
  ZPlaneCanvas.setBounds(5, 5, ScreenSize, ScreenSize);
  ZPlaneCanvas.addMouseListener(this);
  
  recalculate_Eps();
  redrawEpsPlaneFractal();
  redrawEpsPlaneDot();
  EpsPlaneCanvas=new C_Screen();
  EpsPlaneCanvas.MyImage=EpsPlaneImage;
  this.add(EpsPlaneCanvas);
  EpsPlaneCanvas.setBounds(494, 5, ScreenSize, ScreenSize);
  EpsPlaneCanvas.addMouseListener(this);
 }

 
 public void run()
 {
//  More technicalities follow. This function describes how to run
//  animations, and does not need corrections.
  int i;
  try
  {
     if (NewMovie)
     {
      AnimImageSequence=new Image[(AnimBreakPoints-1)*FramesPerSegment];
      AnimGraphicsSequence=new Graphics[(AnimBreakPoints-1)*FramesPerSegment];
      StatusLabel.setVisible(true);
      for(i=0; i<(AnimBreakPoints-1)*FramesPerSegment; i++)
      {
       AnimImageSequence[i]=createImage(ScreenSize, ScreenSize);
       AnimGraphicsSequence[i]=AnimImageSequence[i].getGraphics();
      }
      for(i=0; i<(AnimBreakPoints-1)*FramesPerSegment; i++)
      {
       StatusLabel.setText("Preparing frame "+(i+1)+" of "+(((AnimBreakPoints-1)*FramesPerSegment)+1)+"...");
       RealC=getNthEps_R(i);
       ImagC=getNthEps_I(i);
       recalculate_Z();
       redrawZPlaneFractal(AnimGraphicsSequence[i]);
      }
      StatusLabel.setText("Preparing frame " + (((AnimBreakPoints-1)*FramesPerSegment)+1) + " of " + (((AnimBreakPoints-1)*FramesPerSegment)+1)+"...");
      this.showStatus("Preparing animation: frame " + (((AnimBreakPoints-1)*FramesPerSegment)+1) + " of " + (((AnimBreakPoints-1)*FramesPerSegment)+1)+"...");
      NewMovie=false;
     }
     RealC=AnimXs[AnimBreakPoints-1];
     ImagC=AnimYs[AnimBreakPoints-1];
     recalculate_Z();
     redrawZPlaneFractal(ZPlaneGraphics);
     StatusLabel.setVisible(false);
     StatusLabel.setText("");
     for(i=0; i<(AnimBreakPoints-1)*FramesPerSegment; i++)
     {
      ZPlaneCanvas.MyImage=AnimImageSequence[i];
      ZPlaneCanvas.repaint();
      Thread.sleep(250);
     }
     ZPlaneCanvas.MyImage=ZPlaneImage;
     ZPlaneCanvas.repaint();
     RealCTextField.setText(Double.toString(AnimXs[AnimBreakPoints-1]));
     ImagCTextField.setText(Double.toString(AnimYs[AnimBreakPoints-1]));
     redrawEpsPlaneFractal();
     redrawAnimSequence();
     redrawEpsPlaneDot();
     EpsPlaneCanvas.repaint();
  }
  catch (Exception e) {}
 }
 
 public void actionPerformed(ActionEvent ae)
 {
//  The next function describes reactions to clicks on different
//  buttons. There is again nothing here to be modified;
//  all calculations are deferred to the functions recalculate_Z
//  and recalculate_Eps, to be described later.
  int i, j;
  Double d; 
  Integer k; 
  String s1;

  if (ae.getSource()== RedrawButton)
  {
   RectDrawScreen=0;
   if (RollBackTotal < RollBackMax) { RollBackTotal++; }
   RollBackIndex++;
   if (RollBackIndex>=RollBackMax) RollBackIndex=0;
   ArchZRedraw[RollBackIndex]=false;
   ArchEpsRedraw[RollBackIndex]=false;
   
   d=Double.valueOf(ReZMinTextField.getText());
   if (d.doubleValue()!=Xmin)
   {
    Xmin=d.doubleValue();
    ArchZRedraw[RollBackIndex]=true;
    NewMovie=true;
   }
   ArchXmin[RollBackIndex]=Xmin;

   d=Double.valueOf(ReZMaxTextField.getText());
   if (d.doubleValue()!=Xmax)
   {
    Xmax=d.doubleValue();
    ArchZRedraw[RollBackIndex]=true;
    NewMovie=true;
   }
   ArchXmax[RollBackIndex]=Xmax;

   d=Double.valueOf(ImZMinTextField.getText());
   if (d.doubleValue()!=Ymin)
   {
    Ymin=d.doubleValue();
    ArchZRedraw[RollBackIndex]=true;
    NewMovie=true;
   }
   ArchYmin[RollBackIndex]=Ymin;

   d=Double.valueOf(ImZMaxTextField.getText());
   if (d.doubleValue()!=Ymax)
   {
    Ymax=d.doubleValue();
    ArchZRedraw[RollBackIndex]=true;
    NewMovie=true;
   }
   ArchYmax[RollBackIndex]=Ymax;
   
   k=Integer.valueOf(ZMaxIterTextField.getText());
   if(k.intValue()!=MaxIter)
   {
    MaxIter=k.intValue();
    ArchZRedraw[RollBackIndex]=true;
    NewMovie=true;
   }
   ArchMaxIter[RollBackIndex]=MaxIter;
   
   k=Integer.valueOf(EpsMaxIterTextField.getText());
   if(k.intValue()!=EpsMaxIter)
   {
    EpsMaxIter=k.intValue();
    ArchEpsRedraw[RollBackIndex]=true;
    NewMovie=true;
   }
   ArchEpsMaxIter[RollBackIndex]=EpsMaxIter;
   
   d=Double.valueOf(ReEpsMinTextField.getText());
   if (d.doubleValue()!=EpsPlaneXmin) ArchEpsRedraw[RollBackIndex]=true;
   EpsPlaneXmin=d.doubleValue();
   ArchEpsXMin[RollBackIndex]=EpsPlaneXmin;
   
   d=Double.valueOf(ReEpsMaxTextField.getText());
   if (d.doubleValue()!=EpsPlaneXmax) ArchEpsRedraw[RollBackIndex]=true;
   EpsPlaneXmax=d.doubleValue();
   ArchEpsXMax[RollBackIndex]=EpsPlaneXmax;
   
   d=Double.valueOf(ImEpsMinTextField.getText());
   if (d.doubleValue()!=EpsPlaneYmin) ArchEpsRedraw[RollBackIndex]=true;
   EpsPlaneYmin=d.doubleValue();
   ArchEpsYMin[RollBackIndex]=EpsPlaneYmin;
   
   d=Double.valueOf(ImEpsMaxTextField.getText());
   if (d.doubleValue()!=EpsPlaneYmax) ArchEpsRedraw[RollBackIndex]=true;
   EpsPlaneYmax=d.doubleValue();
   ArchEpsYMax[RollBackIndex]=EpsPlaneYmax;

   d=Double.valueOf(RealCTextField.getText());
   if (d.doubleValue()!=RealC)
   {
    Iterations=0;
    ArchZRedraw[RollBackIndex]=true;
    OrbitList.setVisible(false);
    OrbitList.removeAll();
    RealC=d.doubleValue();
   }
   ArchRealC[RollBackIndex]=RealC;
   
   d=Double.valueOf(ImagCTextField.getText());
   if (d.doubleValue()!=ImagC)
   {
    Iterations=0;
    ArchZRedraw[RollBackIndex]=true;
    OrbitList.setVisible(false);
    OrbitList.removeAll();
    ImagC=d.doubleValue();
   }
   ArchImagC[RollBackIndex]=ImagC;
   
   recalculate_Z();
   recalculate_Eps();
   RectDrawScreen=0;
   redrawZPlaneFractal(ZPlaneGraphics);
   redrawZPlaneOrbit();
   redrawEpsPlaneFractal();
   redrawEpsPlaneDot();
   redrawAnimSequence();
   ZPlaneCanvas.repaint();
   EpsPlaneCanvas.repaint();
   this.showStatus("EpsMaxIter="+Integer.toString(EpsMaxIter));
   return;
  }
  if (ae.getSource()==IterateButton)
  {
   k=Integer.valueOf(IterationsTextField.getText());
   Iterations=k.intValue();
   if (Iterations<=0) return;
   Double d1, d2;
   d1=Double.valueOf(RealIterTextField.getText());
   d2=Double.valueOf(ImagIterTextField.getText());
   SeedX=d1.doubleValue();
   SeedY=d2.doubleValue();
   NewOrbit=false;
   recalculate_Orbit(d1.doubleValue(), d2.doubleValue());
   OrbitList.removeAll();
   for(i=0; i<Iterations; i++)
   {
    if (OrbitYs[i]>=0) s1="+";
    else s1="";
    OrbitList.add(Double.toString(trunc(OrbitXs[i])) + s1 + Double.toString(trunc(OrbitYs[i])) + "i", i);
   }
   redrawZPlaneFractal(ZPlaneGraphics);
   redrawZPlaneOrbit();
   ZPlaneCanvas.repaint();
   OrbitList.setVisible(true);
   return;
  }
  if (ae.getSource()==NextIterButton)
  {
   Double d1, d2;
   if (RealIterTextField.getText()=="") return;
   if (ImagIterTextField.getText()=="") return;
   d1=Double.valueOf(RealIterTextField.getText());
   d2=Double.valueOf(ImagIterTextField.getText());
   if ((d1.doubleValue()!=SeedX)||(d2.doubleValue()!=SeedY)||(NewOrbit)) Iterations=1;
   SeedX=d1.doubleValue();
   SeedY=d2.doubleValue();
   NewOrbit=false;
   Iterations++;
   recalculate_Orbit(d1.doubleValue(), d2.doubleValue());
   OrbitList.removeAll();
   for(i=0; i<Iterations; i++)
   {
    if (OrbitYs[i]>=0) s1="+";
    else s1="";
    OrbitList.add(Double.toString(trunc(OrbitXs[i])) + s1 + Double.toString(trunc(OrbitYs[i])) + "i", i);
   }
   redrawZPlaneFractal(ZPlaneGraphics);
   redrawZPlaneOrbit();
   ZPlaneCanvas.repaint();
   OrbitList.setVisible(true);
   return;
  }
  if (ae.getSource()==AddEpsButton)
  {
   if (AnimBreakPoints==MaxAnim) return;
   NewMovie=true;
   d=Double.valueOf(RealCTextField.getText());
   if (d.doubleValue()<0) s1="";
   else s1="+";
   AnimXs[AnimBreakPoints]=d.doubleValue();
   d=Double.valueOf(ImagCTextField.getText());
   AnimYs[AnimBreakPoints]=d.doubleValue();
   AnimList.add(Double.toString(trunc(AnimXs[AnimBreakPoints]))+s1+Double.toString(trunc(AnimYs[AnimBreakPoints])), AnimBreakPoints);
   AnimBreakPoints++;
   redrawEpsPlaneFractal();
   redrawEpsPlaneDot();
   redrawAnimSequence();
   EpsPlaneCanvas.repaint();
   return;
  }
  if (ae.getSource()==RemoveEpsButton)
  {
   boolean SmthChosen=false;
   for(i=0; i<AnimBreakPoints; i++)
    if (AnimList.isIndexSelected(i)) SmthChosen=true;
   if (!SmthChosen) return;
   NewMovie=true;
   j=AnimList.getSelectedIndex();
   AnimList.remove(j);
   for(i=j+1; i<AnimBreakPoints; i++)
   {
    AnimXs[i-1]=AnimXs[i];
    AnimYs[i-1]=AnimYs[i];
   }
   AnimBreakPoints--;
   redrawEpsPlaneFractal();
   redrawEpsPlaneDot();
   redrawAnimSequence();
   EpsPlaneCanvas.repaint();
   return;
  }
  if (ae.getSource()==AnimateButton)
  {
   if (AnimBreakPoints<=1) return;
   Iterations=0;
   OrbitList.removeAll();
   OrbitList.setVisible(false);
   if (RectDrawScreen==1) RectDrawScreen=0;
   AnimationThread=new Thread(this);
   AnimationThread.start();
   return;
  }
  if (ae.getSource()==RestoreButton)
  {

   RealC=BaseRealC;
   ImagC=BaseImagC;
   Xmin=BaseXmin;
   Xmax=BaseXmax;
   Ymin=BaseYmin;
   Ymax=BaseYmax;
   EpsPlaneXmin=BaseEpsXmin;
   EpsPlaneXmax=BaseEpsXmax;
   EpsPlaneYmin=BaseEpsYmin;
   EpsPlaneYmax=BaseEpsYmax;
   Iterations=0;
   MaxIter=BaseMaxIter;
   EpsMaxIter=BaseEpsMaxIter;
   RectDrawScreen=0;
   RollBackIndex=0;
   RollBackTotal=1;
   ArchRealC[RollBackIndex]=RealC;
   ArchImagC[RollBackIndex]=ImagC;
   ArchXmin[RollBackIndex]=Xmin;
   ArchXmax[RollBackIndex]=Xmax;
   ArchYmin[RollBackIndex]=Ymin;
   ArchYmax[RollBackIndex]=Ymax;
   ArchEpsXMin[RollBackIndex]=EpsPlaneXmin;
   ArchEpsXMax[RollBackIndex]=EpsPlaneXmax;
   ArchEpsYMin[RollBackIndex]=EpsPlaneYmin;
   ArchEpsYMax[RollBackIndex]=EpsPlaneYmax;
   ArchMaxIter[RollBackIndex]=MaxIter;
   ArchEpsMaxIter[RollBackIndex]=EpsMaxIter;
   OrbitList.setVisible(false);
   OrbitList.removeAll();
   NewMovie=true;
   AnimList.removeAll();
   AnimBreakPoints=0;
   RealCTextField.setText(Double.toString(BaseRealC));
   ImagCTextField.setText(Double.toString(BaseImagC));
   ReZMinTextField.setText(Double.toString(BaseXmin));
   ImZMinTextField.setText(Double.toString(BaseYmin));
   ReZMaxTextField.setText(Double.toString(BaseXmax));
   ImZMaxTextField.setText(Double.toString(BaseYmax));
   ReEpsMinTextField.setText(Double.toString(BaseEpsXmin));
   ImEpsMinTextField.setText(Double.toString(BaseEpsYmin));
   ReEpsMaxTextField.setText(Double.toString(BaseEpsXmax));
   ImEpsMaxTextField.setText(Double.toString(BaseEpsYmax));
   RealIterTextField.setText("");
   ImagIterTextField.setText("");
   IterationsTextField.setText("10");
   ZMaxIterTextField.setText(Integer.toString(BaseMaxIter));
   EpsMaxIterTextField.setText(Integer.toString(BaseEpsMaxIter));
   recalculate_Z();
   recalculate_Eps();
   redrawZPlaneFractal(ZPlaneGraphics);
   redrawEpsPlaneFractal();
   redrawEpsPlaneDot();
   ZPlaneCanvas.repaint();
   EpsPlaneCanvas.repaint();
   return;
  }
  if (ae.getSource()==RollBackButton)
  {
   if (RollBackTotal<=1) return;
   else RollBackTotal--;
   i=RollBackIndex;
   if (RollBackIndex>0) RollBackIndex--;
   else { RollBackIndex=(RollBackMax-1); }
   RealC=ArchRealC[RollBackIndex];
   ImagC=ArchImagC[RollBackIndex];
   Xmin=ArchXmin[RollBackIndex];
   Xmax=ArchXmax[RollBackIndex];
   Ymin=ArchYmin[RollBackIndex];
   Ymax=ArchYmax[RollBackIndex];
   EpsPlaneXmin=ArchEpsXMin[RollBackIndex];
   EpsPlaneXmax=ArchEpsXMax[RollBackIndex];
   EpsPlaneYmin=ArchEpsYMin[RollBackIndex];
   EpsPlaneYmax=ArchEpsYMax[RollBackIndex];
   Iterations=0;
   MaxIter=ArchMaxIter[RollBackIndex];
   EpsMaxIter=ArchEpsMaxIter[RollBackIndex];
   ReZMinTextField.setText(Double.toString(ArchXmin[RollBackIndex]));
   ReZMaxTextField.setText(Double.toString(ArchXmax[RollBackIndex]));
   ImZMinTextField.setText(Double.toString(ArchYmin[RollBackIndex]));
   ImZMaxTextField.setText(Double.toString(ArchYmax[RollBackIndex]));
   ReEpsMinTextField.setText(Double.toString(ArchEpsXMin[RollBackIndex]));
   ReEpsMaxTextField.setText(Double.toString(ArchEpsXMax[RollBackIndex]));   
   ImEpsMinTextField.setText(Double.toString(ArchEpsYMin[RollBackIndex]));
   ImEpsMaxTextField.setText(Double.toString(ArchEpsYMax[RollBackIndex]));
   RealCTextField.setText(Double.toString(ArchRealC[RollBackIndex]));
   ImagCTextField.setText(Double.toString(ArchImagC[RollBackIndex]));
   ZMaxIterTextField.setText(Integer.toString(ArchMaxIter[RollBackIndex]));
   EpsMaxIterTextField.setText(Integer.toString(ArchEpsMaxIter[RollBackIndex]));
   RectDrawScreen=0;
   OrbitList.setVisible(false);
   OrbitList.removeAll();
   NewMovie=true;
   AnimList.removeAll();
   AnimBreakPoints=0;
   if (ArchZRedraw[i]) recalculate_Z();
   if (ArchEpsRedraw[i]) recalculate_Eps();
   redrawZPlaneFractal(ZPlaneGraphics);
   redrawEpsPlaneFractal();
   redrawEpsPlaneDot();
   ZPlaneCanvas.repaint();
   EpsPlaneCanvas.repaint();
   return;
  }
 }
 
 public void mouseClicked(MouseEvent me)
 {
//  This and the next four functions implement zooming with a mouse.
//  Remarkably, there is still nothing to be modified!
  double x, y;
  int j;
  if (me.getSource()==ZPlaneCanvas)
  {
   Xincr=(Xmax-Xmin)/ScreenSize;
   Yincr=(Ymax-Ymin)/ScreenSize;
   x=Xmin+(Xincr*me.getX());
   y=Ymin+(Yincr*(ScreenSize-me.getY()));
   RealIterTextField.setText(Double.toString(x));
   ImagIterTextField.setText(Double.toString(y));
   if (me.getClickCount()>1)
   {
    NewOrbit=false;
    SeedX=x;
    SeedY=y;
    Integer i=new Integer(0);
    i=Integer.valueOf(IterationsTextField.getText());
    if (i.intValue()<=0) return;
    Iterations=i.intValue();
    recalculate_Orbit(x, y);
    String s1;
    OrbitList.removeAll();
    for(j=0; j<Iterations; j++)
    {
     if (OrbitYs[j]>=0) s1="+";
     else s1="";
     OrbitList.add(Double.toString(trunc(OrbitXs[j])) + s1 + Double.toString(trunc(OrbitYs[j])) + "i", j);
    }
    redrawZPlaneFractal(ZPlaneGraphics);
    redrawZPlaneOrbit();
    ZPlaneCanvas.repaint();
    OrbitList.setVisible(true);
   }
   else
   {
    redrawZPlaneFractal(ZPlaneGraphics);
    ZPlaneGraphics.setColor(new Color(255, 255, 255));
    ZPlaneGraphics.fillRect(me.getX()-1, me.getY()-1, 3, 3);
    ZPlaneCanvas.repaint();
    NewOrbit=false;
   }
  }
  if (me.getSource()==EpsPlaneCanvas)
  {
   EpsPlaneXincr=(EpsPlaneXmax-EpsPlaneXmin)/ScreenSize;
   EpsPlaneYincr=(EpsPlaneYmax-EpsPlaneYmin)/ScreenSize;
   x=EpsPlaneXmin+(EpsPlaneXincr*me.getX());
   y=EpsPlaneYmin+(EpsPlaneYincr*(ScreenSize-me.getY()));
   RealCTextField.setText(Double.toString(x));
   ImagCTextField.setText(Double.toString(y));
   redrawEpsPlaneFractal();
   redrawEpsPlaneDot();
   redrawAnimSequence();
   EpsPlaneCanvas.repaint();
   if (me.getClickCount()>1)
   {
    if (RollBackTotal<RollBackMax) RollBackTotal++;
    RollBackIndex++;
    if (RollBackIndex>=RollBackMax) RollBackIndex=0;
    ArchXmin[RollBackIndex]=Xmin;
    ArchXmax[RollBackIndex]=Xmax;
    ArchYmin[RollBackIndex]=Ymin;
    ArchYmax[RollBackIndex]=Ymax;
    ArchEpsXMin[RollBackIndex]=EpsPlaneXmin;
    ArchEpsXMax[RollBackIndex]=EpsPlaneXmax;
    ArchEpsYMin[RollBackIndex]=EpsPlaneYmin;
    ArchEpsYMax[RollBackIndex]=EpsPlaneYmax;
    ArchMaxIter[RollBackIndex]=MaxIter;
    ArchEpsMaxIter[RollBackIndex]=EpsMaxIter;
    ArchZRedraw[RollBackIndex]=true;
    ArchEpsRedraw[RollBackIndex]=false;
    RealC=x;
    ImagC=y;
    ArchRealC[RollBackIndex]=RealC;
    ArchImagC[RollBackIndex]=ImagC;
    Iterations=0;
    OrbitList.setVisible(false);
    OrbitList.removeAll();
    if (RectDrawScreen==1) RectDrawScreen=0;
    recalculate_Z();
    redrawZPlaneFractal(ZPlaneGraphics);
    ZPlaneCanvas.repaint();
   }
  }
 }
 
 public void mouseEntered(MouseEvent me)
 {
 }
 
 public void mouseExited(MouseEvent me)
 {
  if (me.getSource()==this) MouseDownScreen=0;
 }
 
 public void mousePressed(MouseEvent me)
 {
  if (me.getSource()==ZPlaneCanvas)
  {
   MouseDownX=me.getX();
   MouseDownY=me.getY();
   MouseDownScreen=1;
   return;
  }
  if (me.getSource()==EpsPlaneCanvas)
  {
   MouseDownX=me.getX();
   MouseDownY=me.getY();
   MouseDownScreen=2;
   return;
  }
  MouseDownScreen=0;
 }
 
 public void mouseReleased(MouseEvent me)
 {
  if (MouseDownScreen==0) return;
  int swap;
  double NewXmin, NewXmax, NewYmin, NewYmax;
  MouseUpX=me.getX();
  MouseUpY=me.getY();
  if (MouseDownX>MouseUpX)
  {
   swap=MouseDownX;
   MouseDownX=MouseUpX;
   MouseUpX=swap;
  }
  if (MouseDownY>MouseUpY)
  {
   swap=MouseDownY;
   MouseDownY=MouseUpY;
   MouseUpY=swap;
  }
  if ((MouseDownX==MouseUpX)||(MouseDownY==MouseUpY))
  {
   MouseDownScreen=0;
   return;
  }
  if (me.getSource()==ZPlaneCanvas)
  {
   if (MouseDownScreen!=1) return;
   MouseDownScreen=0;
   Xincr=(Xmax-Xmin)/ScreenSize;
   Yincr=(Ymax-Ymin)/ScreenSize;
   NewXmin=Xmin+(MouseDownX*Xincr);
   NewYmin=Ymin+((ScreenSize-MouseUpY)*Yincr);
   NewXmax=Xmin+(MouseUpX*Xincr);
   NewYmax=Ymin+((ScreenSize-MouseDownY)*Yincr);
   ReZMinTextField.setText(Double.toString(NewXmin));
   ReZMaxTextField.setText(Double.toString(NewXmax));
   ImZMinTextField.setText(Double.toString(NewYmin));
   ImZMaxTextField.setText(Double.toString(NewYmax));
   NewMovie=true;
   if (RectDrawScreen==1) redrawZPlaneFractal(ZPlaneGraphics);
   if (RectDrawScreen==2)
   {
    redrawEpsPlaneFractal();
    redrawEpsPlaneDot();
    ReEpsMinTextField.setText(Double.toString(EpsPlaneXmin));
    ReEpsMaxTextField.setText(Double.toString(EpsPlaneXmax));
    ImEpsMinTextField.setText(Double.toString(EpsPlaneYmin));
    ImEpsMaxTextField.setText(Double.toString(EpsPlaneYmax));
   }
   RectDrawScreen=1;
   redrawZoomRect();
  }
  if (me.getSource()==EpsPlaneCanvas)
  {
   if (MouseDownScreen!=2) return;
   MouseDownScreen=0;
   EpsPlaneXincr=(EpsPlaneXmax-EpsPlaneXmin)/ScreenSize;
   EpsPlaneYincr=(EpsPlaneYmax-EpsPlaneYmin)/ScreenSize;
   NewXmin=EpsPlaneXmin+(MouseDownX*EpsPlaneXincr);
   NewYmin=EpsPlaneYmin+((ScreenSize-MouseUpY)*EpsPlaneYincr);
   NewXmax=EpsPlaneXmin+(MouseUpX*EpsPlaneXincr);
   NewYmax=EpsPlaneYmin+((ScreenSize-MouseDownY)*EpsPlaneYincr);
   ReEpsMinTextField.setText(Double.toString(NewXmin));
   ReEpsMaxTextField.setText(Double.toString(NewXmax));
   ImEpsMinTextField.setText(Double.toString(NewYmin));
   ImEpsMaxTextField.setText(Double.toString(NewYmax));
   if (RectDrawScreen==1)
   {
    redrawZPlaneFractal(ZPlaneGraphics);
    ReZMinTextField.setText(Double.toString(Xmin));
    ReZMaxTextField.setText(Double.toString(Xmax));
    ImZMinTextField.setText(Double.toString(Ymin));
    ImZMaxTextField.setText(Double.toString(Ymax));
   }
   if (RectDrawScreen==2)
   {
    redrawEpsPlaneFractal();
    redrawEpsPlaneDot();
   }
   RectDrawScreen=2;
   redrawZoomRect();
  }
  ZPlaneCanvas.repaint();
  EpsPlaneCanvas.repaint();
 }
 
// The next seven functions implement the drawing of various components
// of the applet and the two screens. They don't need to be modified,
// but I briefly explain what each of them does.
 
 public void paint(Graphics g)
 {
//  This simple function paints the basic elements of the applet
//  window: the green frame and the green lines separating the
//  portions of the applet from one another.
  g.setColor(new Color(0, 100, 0));
  g.fillRect(0, 0, 799, 5);
  g.fillRect(0, 395, 799, 4);
  g.fillRect(0, 0, 4, 399);
  g.fillRect(795, 0, 4, 399);
  g.fillRect(306, 0, 5, 311);
  g.fillRect(0, 306, 311, 5);
  g.fillRect(488, 0, 5, 311);
  g.fillRect(488, 306, 311, 5);
  g.fillRect(310, 93, 180, 2);
  g.fillRect(310, 229, 180, 2);
  g.fillRect(310, 328, 180, 2);
  g.fillRect(309, 311, 2, 19);
  g.fillRect(488, 311, 2, 19);
 }
 
 public void redrawZPlaneFractal(Graphics g)
 {
//  This function assigns to each pixel in the Z-plane the color
//  stored for it in the ResultArray
  int i, j;
  for(i=0; i<ScreenSize; i++)
   for(j=0; j<ScreenSize; j++)
   {
    g.setColor(numberToColor(ResultArray[i][j], true));
    g.drawLine(i, 300-j, i, 300-j);
   }
 }
 
 public void redrawZoomRect()
 {
//  This draws the white rectangle that pops out when the user
//  zooms with a mouse, either in the Z- or in the Eps-screen.
  switch (RectDrawScreen)
  {
  case 1:
   {
    ZPlaneGraphics.setColor(new Color(255, 255, 255));
    ZPlaneGraphics.drawRect(MouseDownX, MouseDownY, (MouseUpX-MouseDownX), (MouseUpY-MouseDownY));
    break;
   }
  case 2:
   {
    EpsPlaneGraphics.setColor(new Color(255, 255, 255));
    EpsPlaneGraphics.drawRect(MouseDownX, MouseDownY, (MouseUpX-MouseDownX), (MouseUpY-MouseDownY));
    break;
   }
  }
 }
 
 public void redrawZPlaneOrbit()
 {
//  This shows the orbit of the selected point in the Z plane.  
  if (Iterations<=0) return;
  int i, x, y;
  Double d;
  Xincr=(Xmax-Xmin)/ScreenSize;
  Yincr=(Ymax-Ymin)/ScreenSize;
  ZPlaneGraphics.setColor(new Color(255, 255, 255));
  for(i=0; i<Iterations; i++)
  {
   d=new Double((OrbitXs[i]-Xmin)/Xincr);
   x=d.intValue();
   d=new Double((OrbitYs[i]-Ymin)/Yincr);
   y=d.intValue();
   if ((0<=x)&&(x<=ScreenSize)&&(0<=y)&&(y<=ScreenSize))
    ZPlaneGraphics.fillRect(x-1, (ScreenSize-y)-1, 3, 3);
  }
 }
 
 public void redrawEpsPlaneFractal()
 {
//  This draws the fractal in the parameter plane, again assigning
//  to each pixel the color stored for it in the EpsPlaneResultArray.  
  int i, j;
  for(i=0; i<ScreenSize; i++)
   for(j=0; j<ScreenSize; j++)
   {
    EpsPlaneGraphics.setColor(numberToColor(EpsPlaneResultArray[i][j], false));
    EpsPlaneGraphics.drawLine(i, ScreenSize-j, i, ScreenSize-j);
   }
 }
 
 public void redrawEpsPlaneDot()
 {
//  This draws the white dot that appears when the user clicks a point
//  in the parameter plane. If the user then clicks "Redraw", the fractal
//  corresponding to that value of the parameter is displayed in the Z-plane.
  int x, y;
  Double d, f;
  EpsPlaneXincr=(EpsPlaneXmax-EpsPlaneXmin)/ScreenSize;
  EpsPlaneYincr=(EpsPlaneYmax-EpsPlaneYmin)/ScreenSize;
  f=new Double(RealCTextField.getText());
  d=new Double((f.doubleValue()-EpsPlaneXmin)/EpsPlaneXincr);
  x=d.intValue();
  f=new Double(ImagCTextField.getText());
  d=new Double((f.doubleValue()-EpsPlaneYmin)/EpsPlaneYincr);
  y=d.intValue();
  EpsPlaneGraphics.setColor(new Color(255, 255, 255));
  if ( (2<=x)&&(x<(ScreenSize-2))&&(2<=y)&&(y<=(ScreenSize-2)) )
   EpsPlaneGraphics.fillRect(x-2, (ScreenSize-y)-2, 5, 5);
 }
 
 public void redrawAnimSequence()
 {
//  Draws the chain of parameter values prepared for the animation.  
  int i;
  boolean PrevPointOnScreen=false;
  int PrevPointX=0, PrevPointY=0, PointX, PointY;
  EpsPlaneXincr=(EpsPlaneXmax-EpsPlaneXmin)/ScreenSize;
  EpsPlaneYincr=(EpsPlaneYmax-EpsPlaneYmin)/ScreenSize;
  EpsPlaneGraphics.setColor(new Color(255, 255, 255));
  for(i=0; i<AnimBreakPoints; i++)
  {
   PointX=(int)Math.round((AnimXs[i]-EpsPlaneXmin)/EpsPlaneXincr);
   PointY=ScreenSize-((int)Math.round((AnimYs[i]-EpsPlaneYmin)/EpsPlaneYincr));
   if ((0<=PointX)&&(PointX<ScreenSize)&&(0<=PointY)&&(PointY<=ScreenSize))
   {
    EpsPlaneGraphics.fillRect(PointX-1, PointY-1, 3, 3);
    if (PrevPointOnScreen) EpsPlaneGraphics.drawLine(PointX, PointY, PrevPointX, PrevPointY);
    PrevPointOnScreen=true;
   }
   else PrevPointOnScreen=false;
   PrevPointX=PointX;
   PrevPointY=PointY;
  }
 }
 
//Next come three auxillary functions, which again can be left unchanged.
 private double getNthEps_R(int n) 
 {
//  This function (and its twin, which follows) implements a simple
//  computation that the program needs when preparing an animation.
  int segm, part;
  if (n>=((AnimBreakPoints-1)*FramesPerSegment)) return 0;
  if (n<0) return 0;
  segm=n/FramesPerSegment;
  part=n%FramesPerSegment;
  return ((AnimXs[segm]*(FramesPerSegment-part))+(AnimXs[segm+1]*part))/((double)FramesPerSegment);
 }

 private double getNthEps_I(int n) 
 {
  int segm, part;
  if (n>=((AnimBreakPoints-1)*FramesPerSegment)) return 0;
  if (n<0) return 0;
  segm=n/FramesPerSegment;
  part=n%FramesPerSegment;
  return ((AnimYs[segm]*(FramesPerSegment-part))+(AnimYs[segm+1]*part))/((double)FramesPerSegment);
 }
 
 public Color numberToColor(int number, boolean ForZ)
 {
/*  This turns a number (the number of iterations it took a given
  point to escape) into an RGB color in which that point must be
  painted. */
  float f;
  if (ForZ) f=(float)number/MaxIter;
  else f=(float)number/EpsMaxIter;
  if (f<0) return null;
  if (f>1) return null;
  if (f<0.5) return (new Color(Color.HSBtoRGB(f, 1, 1)));
  else return (new Color(Color.HSBtoRGB(f, 1, 2-(2*f))));
//ATTENTION: This is highly optional, but by tweaking the formulas
//  in the last two lines you can produce different color patterns.
//  Make sure all three numbers in the parentheses stay between
//  0 and 1. f is a real number between 0 and 1; the closer it
//  is to 1, the longer it took the point to escape.
 }
 
/*Here is where the technical part ends and the math begins. Briefly
 speaking, we now have specified all the parameters, and are in a
 position to do the calculations. The workhorses of the applet are
 iterFunction_R and iterFunction_I, which need to be completely
 rewritten when you change the function. As the name suggests,
 they implement the real and imaginary parts of the function being
 iterated. recalculate_Z and recalculate_Orbit describe the actual
 iteration algorithm; they can be left the same. However,
 recalculate_Eps also needs to be modified, because the algorithm
 for computing the critical points is different for different functions.
*/ 
 private void recalculate_Z()
 {
//  This function says, "Iterate each of the 90,000 points in question
//  until it escapes beyond CritRadius or you make MaxIter iterations,
//  whichever comes first. Once done, store the resulting number of
//  iterations in ResultArray".
  int i, j, k;
  double CurrentXCoord, CurrentYCoord;
  double NewR, NewI, OldR, OldI;
  Xincr=(Xmax-Xmin)/ScreenSize;
  Yincr=(Ymax-Ymin)/ScreenSize;
  CurrentXCoord=Xmin;
  CurrentYCoord=Ymin;
  for(i=0; i < ScreenSize; i++)
  {
   for(j=0; j < ScreenSize; j++)
   {
    k=0;
    OldR=CurrentXCoord;
    OldI=CurrentYCoord;
    while (  ( sqr(OldR)+sqr(OldI) < sqr(CritRadius) ) && (k<MaxIter) )
    {
     k++;
     NewR=iterFunction_R(OldR, OldI);
     NewI=iterFunction_I(OldR, OldI);
     OldR=NewR;
     OldI=NewI;
    }
    ResultArray[i][j]=k;
    CurrentYCoord+=Yincr;
   }
   CurrentYCoord=Ymin;
   CurrentXCoord+=Xincr;
  }
 }
 
 private void recalculate_Orbit(double r0, double i0)
 {
//  "Iterate the selected point as many times as the user
//  told you to, and store the results in OrbitXs and OrbitYs".
  if (Iterations<=0) return;
  OrbitXs=new double[Iterations];
  OrbitYs=new double[Iterations];
  int i2=1;
  OrbitXs[0]=r0;
  OrbitYs[0]=i0;
  while (i2<Iterations)
  {
   OrbitXs[i2]=this.iterFunction_R(OrbitXs[i2-1], OrbitYs[i2-1]);
   OrbitYs[i2]=this.iterFunction_I(OrbitXs[i2-1], OrbitYs[i2-1]);
   if (sqr(OrbitXs[i2])+sqr(OrbitYs[i2])>sqr(CritRadius))
   {
    Iterations=i2+1;
    return;
   }
   i2++;
  }
 }
 
 private void recalculate_Eps()
 {
/*  This function needs to be changed. In fact, it can be
  one of the trickiest places to change in the entire applet,
  although the change is trivial for our polynomials: you just have
  to change the value of the critical point.
  
  The plan is to draw the fractal in the parameter plane by finding
  a critical point of the function for each value of the parameter,
  and then setting the parameter to that value and iterating the
  critical point until it escapes (or does not escape).
  
  The question is: first, how do we find a critical point? In general,
  it may involve solving a complicated equation. Fortunately, for all
  the polynomial functions we're interested in, the critical point
  does not depend on the parameter, making our life much easier.
  Second, what if there is not one but many critical points? This
  never arises for z^2+c, as there is only one. For C(z-z^3/3), and
  for z^n+ E/z^m, a very fortunate cicrumstance helps us out: all
  critical points are conjugate to each other, so if one escapes,
  then so do all others.

  For Cz(1-z)(1-2z)^2, we also manage to get away easily. The three
  critical points are at 1/2, 1/2+sqrt(2)/4 and 1/2-sqrt(2)/4.
  As for 1/2, it gets mapped to 0 and stays there for all time,
  regardless of whether 0 is attracting or repelling. Fortunately,
  when 0 is attracting (i.e. abs(C)< 1), it attracts the other two
  critical points as well. So we only have to worry about
  the other two critical points. But an easy computation shows that
  our function maps them to one and the same point. So it is again
  sufficient to iterate just one critical point, 1/2 + sqrt(2)/4.
  
  In general, however, you may need to iterate multiple critical
  points. That is not too hard: you just have to insert additional
  "while" cycles inside the second "for" cycle. After they all run,
  you pick the largest of the results (it is convenient to use
  different integer variables, e.g. k1, k2, etc., for different
  "while" cycles). Then you just write that largest result in the
  EpsPlaneResultArray.
  
  What's harder is to compute the critical point in the cases when
  it does depend on the parameter. This can be a huge problem for
  a general function. We are lucky to not have encountered it yet.*/
  
  int i, j, k;
  double NewR1, NewI1, OldR1, OldI1, z, t, NewR2, NewI2, OldR2, OldI2;
  double GoodOldRealC, GoodOldImagC, r1, theta1, r2, theta2;
/*  GoodOldRealC and GoodOldImagC keep the values of RealC and ImagC*/
  GoodOldRealC=RealC;
  GoodOldImagC=ImagC;
  RealC=EpsPlaneXmin;
  ImagC=EpsPlaneYmin;
  EpsPlaneXincr=(EpsPlaneXmax-EpsPlaneXmin)/ScreenSize;
  EpsPlaneYincr=(EpsPlaneYmax-EpsPlaneYmin)/ScreenSize;
  for(i=0; i < ScreenSize; i++)
  {
   for(j=0; j < ScreenSize; j++)
   {
/*ATTENTION: Here is the only place in recalculate_Eps where you need to
change anything (in the case of our polynomials). You have to enter the
value of the critical point in OldR and OldI. In fact, OldI will always
be zero, as our critical points are all real.
So, for C(z-z^3/3), change OldR to 1.
For Cz(1-z)(1-2z)^2, change OldR to 1/2 + sqrt(2)/4, which is
approximately equal to 0.85355339059327376220042218105242 .
The applet will run fine if you truncate this, say, at the tenth digit.
 */ 
    //Iteration of first critical point
    r1 = Math.sqrt(RealC*RealC + ImagC*ImagC);
    theta1 = Math.atan2(ImagC, RealC);
    OldR1=-((Math.sqrt(r1))*(Math.cos(theta1/2)));
    OldI1=-((Math.sqrt(r1))*(Math.sin(theta1/2)));
    k=0;
    while ((sqr(OldR1)+sqr(OldI1)<sqr(CritRadius)) && (k<EpsMaxIter) )
    {
     NewR1=iterFunction_R(OldR1, OldI1);
     NewI1=iterFunction_I(OldR1, OldI1);
     OldR1=NewR1;
     OldI1=NewI1;
     k++;
    }
    EpsPlaneResultArray[i][j]=k;
    ImagC+=EpsPlaneYincr;
   }
   ImagC=EpsPlaneYmin;
   RealC+=EpsPlaneXincr;
  }
  RealC=GoodOldRealC;
  ImagC=GoodOldImagC;
 }

 private double iterFunction_R(double ReX, double ImX)
 {
/* ATTENTION: Here is where you need to specify your new function.
You are given the value of the variable as ReX and ImX, which are the
real and imaginary parts, respectively. The value of the parameter is
given by RealC and ImagC. The goal is to return the real value of f(z)
in iterFunction_R, and the imaginary value in iterFunction_I. For
that you can use basic calculations in C++ (using +, -, * and /), and
the helpful functions complexMult_R, complexMult_I, complexDiv_R
and complexDiv_I. If you don't want to use the last four functions,
you do not have to; it will only make the computations slightly more
elongated.
 If you don't use complexMult and complexDiv, it helps to write out
the computations with complex numbers on a piece of paper.*/ 
 
  double xSquaredRe, xSquaredIm,xCubedRe, x1, xFinal, xtimesCRe, xtimesCIm;
  
  xSquaredRe=complexMult_R(ReX, ImX, ReX, ImX);
  xSquaredIm=complexMult_I(ReX, ImX, ReX, ImX);
  xCubedRe=complexMult_R(ReX, ImX, xSquaredRe, xSquaredIm);
  xtimesCRe=complexMult_R(RealC, ImagC, ReX, ImX);
  xtimesCIm=complexMult_I(RealC, ImagC, ReX, ImX);
  xFinal=xCubedRe-complexMult_R(3, 0, xtimesCRe, xtimesCIm);
  return xFinal;
  
      
/*  This is the original implimentation of the quadratic map
  double x1;
  x1=complexMult_R(ReX, ImX, ReX, ImX);
  x1=x1+RealC;
  return x1; */
 
 }
 
 private double iterFunction_I(double ReX, double ImX)
 {
/*ATTENTION: Typically you perform the same computations here as
you did in iterFunction_R, but return the imaginary part of the
result. I put the "backup iterFunctions" for C(z-z^3/3) at the
end of this function as well.*/
  double xSquaredRe, xSquaredIm,xCubedIm, x1, xFinal, xtimesCRe, xtimesCIm;
  
  xSquaredRe=complexMult_R(ReX, ImX, ReX, ImX);
  xSquaredIm=complexMult_I(ReX, ImX, ReX, ImX);
  xCubedIm=complexMult_I(ReX, ImX, xSquaredRe, xSquaredIm);
  xtimesCRe=complexMult_R(RealC, ImagC, ReX, ImX);
  xtimesCIm=complexMult_I(RealC, ImagC, ReX, ImX);
  xFinal=xCubedIm-complexMult_I(3, 0, xtimesCRe, xtimesCIm);
  return xFinal;

  
/* This is the original quadratic map
  int i;
  double y1;
  y1=complexMult_I(ReX, ImX, ReX, ImX);
  y1=y1+ImagC;
  return y1; */

 }
 
// The last few functions are trivial. They implement complex
// multiplication, complex division, truncation of real numbers and squaring.

 public double complexMult_R(double r1, double i1, double r2, double i2)
 {
  return ( (r1*r2)-(i1*i2) );
 }
 
 public double complexMult_I(double r1, double i1, double r2, double i2)
 {
  return ( (r1*i2)+(i1*r2) );
 }
 
 public double complexDiv_R(double r1, double i1, double r2, double i2)
 {
  return ( ((r1*r2)+(i1*i2))/(sqr(r2)+sqr(i2)) );
 }
 
 public double complexDiv_I(double r1, double i1, double r2, double i2)
 {
  return ( (i1*r2-r1*i2)/(sqr(r2)+sqr(i2)) );
 }
 
 public double sqr(double x)
 {
  return (x*x);
 }
 
 public double trunc(double x)
 {
  return (Math.rint(x*1e6)/1e6);
 }
}