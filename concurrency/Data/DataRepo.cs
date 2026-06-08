using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;

namespace Data
{
    public class DataRepo
    {
        private ObservableCollection<BallData> _balls = new ObservableCollection<BallData>();

        public ObservableCollection<BallData> Balls
        {
            get { return _balls; }
            set
            {
                _balls = value;
            }
        }
    }
}
