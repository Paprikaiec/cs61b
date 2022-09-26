public class Body {
    public double xxPos, yyPos, xxVel, yyVel, mass;
    public String imgFileName;
    static final double G = 6.67e-11;

    public Body(double xP, double yP, double xV, double yV, 
                double m, String img){
        xxPos = xP; 
        yyPos = yP; 
        xxVel = xV;
        yyVel = yV;
        mass = m;
        imgFileName = img; 
    }

    public Body(Body b){
        xxPos = b.xxPos;
        yyPos = b.yyPos;
        xxVel = b.xxVel;
        yyVel = b.yyVel;
        mass = b.mass;
        imgFileName = b.imgFileName;
    }

    public double calcDistance(Body b){
        return Math.sqrt(Math.pow(this.xxPos - b.xxPos, 2) + 
                         Math.pow(this.yyPos - b.yyPos, 2));
    }

    public double calcForceExertedBy(Body b){
        double r = this.calcDistance(b);
        return G * this.mass * b.mass / Math.pow(r, 2);
    }

    public double calcForceExertedByX(Body b){
       double dx = b.xxPos - this.xxPos;
       double F = this.calcForceExertedBy(b);
       double r = this.calcDistance(b);
       return F * dx / r;
        
    }

     public double calcForceExertedByY(Body b){
       double dy = b.yyPos - this.yyPos;
       double F = this.calcForceExertedBy(b);
       double r = this.calcDistance(b);
       return F * dy / r;
        
    }

    public double calcNetForceExertedByX(Body[] b_list){
        double Fx = 0;
        for (Body b: b_list){
            if (this.equals(b)) continue;
            Fx += this.calcForceExertedByX(b);
        }
        return Fx;
    }

    public double calcNetForceExertedByY(Body[] b_list){
        double Fy = 0;
        for (Body b: b_list){
            if (this.equals(b)) continue;
            Fy += this.calcForceExertedByY(b);
        }
        return Fy;
    }

    public void update(double dt, double Fx, double Fy){
        double ax = Fx / this.mass;
        double ay = Fy / this.mass;
        this.xxVel += dt * ax;
        this.yyVel += dt * ay;
        this.xxPos += dt * this.xxVel;
        this.yyPos += dt * this.yyVel; 
    }

    public void draw(){
        StdDraw.picture(this.xxPos, this.yyPos, "images/"+this.imgFileName);
    }
}
