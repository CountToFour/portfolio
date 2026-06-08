using Model;
using Data;
using System;
using System.Threading;
using System.Collections.Generic;
using System.Threading.Tasks;
using System.Text.Json;
using System.IO;
using System.Linq;
using System.Timers;

namespace Logic
{
    public interface IBallLogic
    {
        public Task CreateBalls();
        DataRepo RepoData { get; set; }
        ModelRepo RepoModel { get; set; }
    }

    public class BallLogic : IBallLogic
    {
        private double _maxWidth = 582;
        private double _maxHeight = 282;
        private readonly Random _random = new Random();
        private ModelRepo _repoModel = new ModelRepo();
        private DataRepo _repoData = new DataRepo();
        private List<Task> _tasks = new List<Task>();
        private CancellationTokenSource _cancellationTokenSource = new CancellationTokenSource();
        private System.Timers.Timer aTimer;
        FileStream filestream;
       
        private void GenerateDirection(object obj1, object obj2)
        {
            BallModel ball = (BallModel)obj1;
            BallData ballData = (BallData)obj2;
            int rand = _random.Next(0, 2);
            int directionHorizontal = rand == 0 ? -1 : 1;
            int directionVertical = rand == 0 ? -1 : 1; 
            ballData.Weight = _random.Next(1, 4);
            ballData.VelocityX = (_random.NextDouble() + 2) * directionHorizontal;
            ballData.VelocityY = (_random.NextDouble() + 2) * directionVertical;
        }
        

        private async Task MoveBall(BallModel ball, BallData ballData, CancellationToken cancellationToken)
        {
            while (!cancellationToken.IsCancellationRequested)
            {
                double newX, newY;
                
                await Task.Delay(10, cancellationToken);

                lock (RepoData.Balls)
                {
                    lock (RepoModel.Balls)
                    {
                        lock (ballData)
                        {
                            int counter = 0;
                            foreach (var otherBall in RepoData.Balls)
                            {
                                double distance = Math.Sqrt(Math.Pow((RepoModel.Balls[counter].PosX - ball.PosX), 2)
                                                           + Math.Pow((RepoModel.Balls[counter].PosY - ball.PosY), 2));
                                if (distance < 21 && otherBall != ballData)
                                {
                                    double tempX = (ballData.VelocityX * (ballData.Weight - otherBall.Weight)
                                                    + 2 * otherBall.Weight * otherBall.VelocityX) /
                                                   (ballData.Weight + otherBall.Weight);
                                    double tempY = (ballData.VelocityY * (ballData.Weight - otherBall.Weight)
                                                    + 2 * otherBall.Weight * otherBall.VelocityY) /
                                                   (ballData.Weight + otherBall.Weight);

                                    otherBall.VelocityX = (otherBall.VelocityX * (otherBall.Weight - ballData.Weight)
                                                           + 2 * ballData.Weight * ballData.VelocityX) /
                                                          (ballData.Weight + otherBall.Weight);
                                    otherBall.VelocityY = (otherBall.VelocityY * (otherBall.Weight - ballData.Weight)
                                                           + 2 * ballData.Weight * ballData.VelocityY) /
                                                          (ballData.Weight + otherBall.Weight);

                                    ballData.VelocityX = tempX;
                                    ballData.VelocityY = tempY;
                                    if (distance < 20)
                                    {
                                        newX = ball.PosX + ballData.VelocityX * 2;
                                        newY = ball.PosY + ballData.VelocityY * 2;
                                        if (newX >= 10 && newX <= _maxWidth && newY >= 10 && newY <= _maxHeight)
                                        {
                                            ball.PosX = newX;
                                            ball.PosY = newY;
                                        }
                                    }
                                }

                                counter++;

                            }

                            newX = ball.PosX + ballData.VelocityX;
                            newY = ball.PosY + ballData.VelocityY;
                            if (newX >= 10 && newX <= _maxWidth && newY >= 10 && newY <= _maxHeight)
                            {
                                ball.PosX = newX;
                                ball.PosY = newY;
                            }
                            else
                            {
                                ballData.VelocityY = -ballData.VelocityY;
                                if (newX < 10 || newX > _maxWidth)
                                {
                                    ballData.VelocityX = -ballData.VelocityX;
                                    ballData.VelocityY = -ballData.VelocityY;
                                }
                            }
                        }
                    }
                }
            }
        }
           
        public async Task CreateBalls()
        {
            RepoData.Balls.Clear();
            RepoModel.Balls.Clear();
            
            _cancellationTokenSource.Cancel();

            int ballsNumber = Convert.ToInt32(RepoModel.BallsNumber);

            _cancellationTokenSource = new CancellationTokenSource();

            _tasks.Clear();

            for (int i = 0; i < ballsNumber; i++)
            {
                BallModel ball = new BallModel();
                BallData ballData = new BallData();
                ball.PosX = _random.Next(10, (int)(_maxWidth));
                ball.PosY = _random.Next(10, (int)(_maxHeight));
                RepoModel.Balls.Add(ball);
                RepoData.Balls.Add(ballData);
                GenerateDirection(ball, ballData);
                Task task = MoveBall(ball, ballData, _cancellationTokenSource.Token);
                _tasks.Add(task);
            }
            
            Task timer = new Task(() => {startLogging(); });
            timer.Start();
            Console.WriteLine(_tasks.Count);
        }
        
        public void startLogging()
        {
            string path = "..\\..\\..\\..\\Logs";

            try
            {
                if (Directory.Exists(path))
                {
                    string fileName = "..\\..\\..\\..\\Logs\\log.json";
                    filestream = File.Create(fileName);
                }
                else
                {
                    Directory.CreateDirectory(path);
                    string fileName = "..\\..\\..\\..\\Logs\\log.json";
                    filestream = File.Create(fileName);
                }
            }
            catch (Exception ex) { Console.WriteLine($"Error: {ex.Message}"); }

            aTimer = new System.Timers.Timer();
            aTimer.Elapsed += new ElapsedEventHandler(SaveLog);
            
            aTimer.Interval = 1000; 
            aTimer.Start();
        }
        public void SaveLog(object data, ElapsedEventArgs e)
        {
            foreach (BallData b in _repoData.Balls)
            {
                JsonSerializer.SerializeAsync(filestream, b);
            }
        }
        
        public ModelRepo RepoModel
        {
            get { return _repoModel; }
            set
            {
                _repoModel = value;
            }
        }

        public DataRepo RepoData
        {
            get { return _repoData; }
            set
            {
                _repoData = value;
            }
        }

    }
}
