public class NBody {
    public static double readRadius(String file){
        In in = new In(file);
        in.readDouble();
        return in.readDouble();
    }
    
    public static Body[] readBodies(String file){
        In in = new In(file);
        int N = in.readInt();
        Body[] bodies = new Body[N];
        in.readDouble();
        for (int i=0; i<N; i+=1){
           bodies[i] = new Body(in.readDouble(), in.readDouble(), 
                                in.readDouble(), in.readDouble(), in.readDouble(), 
                                in.readString());

        } 
        return bodies;
    }
    
    public static void main(String[] args){
        double T = Double.parseDouble(args[0]);
        double dt = Double.parseDouble(args[1]);
        String filename = args[2];
        double radius = readRadius(filename);
        Body[] bodies = readBodies(filename);

        // /*Drawing the background */
        // StdDraw.enableDoubleBuffering();
        // StdDraw.setScale(-radius, radius);
        // StdDraw.clear();
        // StdDraw.picture(0, 0, "images/starfield.jpg");

        // /*Drawing more than one Body */
        // for (Body b: bodies){
        //    b.draw(); 
        // }
        
        /*Creating an Animation */
        double time = 0;
        while (time <= T){
            double[] xForces = new double[bodies.length];
            double[] yForces = new double[bodies.length];
            for (int i=0; i<bodies.length; i+=1){
                xForces[i] = bodies[i].calcNetForceExertedByX(bodies);
                yForces[i] = bodies[i].calcNetForceExertedByY(bodies);
                
            }
           
            for (int i=0; i<bodies.length; i+=1){
                bodies[i].update(dt, xForces[i], yForces[i]);
                
            }
            
            /*Drawing */
            StdDraw.enableDoubleBuffering();
            StdDraw.setScale(-radius, radius);
            StdDraw.clear();
            StdDraw.picture(0, 0, "images/starfield.jpg");
            for (Body b: bodies){
                b.draw(); 
             }
            
            StdDraw.show();
            StdDraw.pause(5);
            
            time +=dt;

        }
    }
}
