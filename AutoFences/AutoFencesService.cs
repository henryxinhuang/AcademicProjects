﻿using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Android.App;
using Android.Content;
using Android.OS;
using Android.Runtime;
using Android.Views;
using Android.Widget;
using Mojio;
using Mojio.Client;

namespace AutoFences
{
    [Service]
    public class AutoFencesService : Service
    {
        private AutoFencesServiceBinder binder;
        private MojioClient client;

        public override StartCommandResult OnStartCommand (Intent intent, StartCommandFlags flags, int startId)
        {
            var prefs = Application.Context.GetSharedPreferences ("settings", FileCreationMode.Private);
            client = AFLib.Globals.client;

            if (client == null) {
                AFLib.MojioConnectionHelper.setupMojioConnection (prefs);
                client = AFLib.Globals.client;
            }

            SignalRHelper.SignalRSetup (client, prefs);
            return StartCommandResult.Sticky;
        }

        public override  Android.OS.IBinder OnBind (Android.Content.Intent intent)
        {
            binder = new AutoFencesServiceBinder (this);
            return binder;
        }

        public class AutoFencesServiceBinder : Binder
        {
            AutoFencesService service;

            public AutoFencesServiceBinder (AutoFencesService service)
            {
                this.service = service;
            }

            public AutoFencesService GetAutoFencesService ()
            {
                return service;
            }
        }
    }
}

